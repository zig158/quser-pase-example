package com.universalinstaller;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileOutputStream;

public class IFSUnpacker {
    List<IFSUnpackerFile> files;
    long fileCount;
    long dirCount;
    final AS400 connection;
    final int ccsid = 1252;
    List<JProgressBar> pBarList = new ArrayList<JProgressBar>(1);
    List<JLabel> pLabelList = new ArrayList<JLabel>(1);
    long lastupdate = 0;
    long byteCount = 0;

    public IFSUnpacker(List<IFSUnpackerFile> files, String Host, String User, String Password) throws IFSUnpackerException {
        this.files = files;
        if (Host == null || Host.trim().equalsIgnoreCase("")) {
            throw new IFSUnpackerException("Error iSeries Host Name is a Required");
        }
        if (User == null || User.trim().equalsIgnoreCase("")) {
            throw new IFSUnpackerException("Error iSeries User Name is a Required");
        }
        if (Password == null || Password.trim().equalsIgnoreCase("")) {
            throw new IFSUnpackerException("Error iSeries Password is a Required");
        }
        for(IFSUnpackerFile installFile : this.files){
            if (!installFile.file.exists()) {
                throw new IFSUnpackerException(String.format("Error Package %s NOT Found", installFile.file.getAbsoluteFile()));
            }
            if (installFile.file.canRead()) continue;
            throw new IFSUnpackerException(String.format("Error Unable To Read Package %s", installFile.file.getAbsoluteFile()));
        }
        try {
            this.connection = new AS400();
            this.connection.setGuiAvailable(false);
            this.connection.setSystemName(Host);
            this.connection.validateSignon(User, Password);
            this.connection.setUserId(User);
            this.connection.setPassword(Password);
        }
        catch (PropertyVetoException e) {
            throw new IFSUnpackerException("Error Connecting to IFS, Property Vetoed", e);
        }
        catch (AS400SecurityException e) {
            throw new IFSUnpackerException("Error Connecting to IFS, Check Username and Password", (Throwable)e);
        }
        catch (IOException e) {
            throw new IFSUnpackerException("Error Connecting to IFS", e);
        }
    }

    public void runInstall() throws IFSUnpackerException {
        try {
            this.getFileCount();
            this.extract();
        }
        finally {
            this.connection.disconnectAllServices();
        }
    }

    public void runRemove() throws IFSUnpackerException {
        try {
            this.getFileCount();
            this.remove();
        }
        finally {
            this.connection.disconnectAllServices();
        }
    }

    public void addProgressBar(JProgressBar progressbar) {
        if (progressbar != null) {
            this.pBarList.add(progressbar);
        }
    }

    public void addStatusLabel(JLabel m_status) {
        if (m_status != null) {
            this.pLabelList.add(m_status);
        }
    }

    private void incrementByteCount(int bytes) {
        this.byteCount+=(long)bytes;
        if (bytes == 0 || System.currentTimeMillis() - this.lastupdate > 50) {
            this.lastupdate = System.currentTimeMillis();
            for (JProgressBar bar : this.pBarList) {
                bar.setString(String.format("%s Bytes Copied", this.byteCount));
            }
        }
    }

    private void incrementProgressBars(long count, String status) {
        if (count > -1) {
            for (JProgressBar bar : this.pBarList) {
                bar.setValue((int)count);
            }
            for (JLabel label : this.pLabelList) {
                label.setText(status);
            }
        }
    }

    private void getFileCount() throws IFSUnpackerException {
        int i;
        long Filecount = 0;
        long Dircount = 0;
        for(IFSUnpackerFile installFile : this.files){
            if (installFile.isZip.booleanValue()) {
                ZipInputStream zipstream = null;
                try {
                    try {
                        zipstream = new ZipInputStream(new FileInputStream(installFile.file));
                        ZipEntry zipentry = null;
                        while ((zipentry = zipstream.getNextEntry()) != null) {
                            this.incrementProgressBars(Filecount, "Getting File Count");
                            if (!zipentry.isDirectory()) {
                                ++Filecount;
                                continue;
                            }
                            ++Dircount;
                        }
                        continue;
                    }
                    catch (FileNotFoundException e) {
                        throw new IFSUnpackerException("Error File NOT Found: " + installFile.file.getAbsolutePath(), e);
                    }
                    catch (IOException e) {
                        throw new IFSUnpackerException("Error Reading File: " + installFile.file.getAbsolutePath(), e);
                    }
                }
                finally {
                    if (zipstream != null) {
                        try {
                            zipstream.close();
                        }
                        catch (IOException e) {}
                    }
                }
            }
            if (installFile.file == null || installFile.ifsFile == null) continue;
            ++Filecount;
        }
        for (i = 0; i < this.pBarList.size(); ++i) {
            this.pBarList.get(i).setValue(0);
            this.pBarList.get(i).setMinimum(0);
            this.pBarList.get(i).setMaximum((int)Filecount);
        }
        this.fileCount = Filecount;
        this.dirCount = Dircount;
    }

    private void extract() throws IFSUnpackerException {
        this.byteCount = 0;
        long count = 0;
        byte[] buffer = new byte[32768];
        String file = "";
        for(IFSUnpackerFile installFile : this.files){
            int len;
            if (installFile.isZip.booleanValue()) {
                ZipInputStream zipstream = null;
                try {
                    try {
                        zipstream = new ZipInputStream(new FileInputStream(installFile.file));
                        ZipEntry zipentry = null;
                        while ((zipentry = zipstream.getNextEntry()) != null) {
                            if (!zipentry.isDirectory()) {
                                file = "/" + zipentry.getName();
                                this.incrementProgressBars(++count, "Copying: " + file);
                                if (installFile.replace != null && !installFile.replace.booleanValue() && new IFSFile(this.connection, file).exists()) 
                                	continue;
                                try(OutputStream output = new BufferedOutputStream(new IFSFileOutputStream(this.connection, file, 1252))){
                                len = 0;
                                while ((len = zipstream.read(buffer)) >= 0) {
                                    output.write(buffer, 0, len);
                                    this.incrementByteCount(len);
                                }
                                }
                            } else {
                                file = "/" + zipentry.getName();
                                IFSFile ifsfile = new IFSFile(this.connection, file);
                                if (!ifsfile.exists()) {
                                    this.incrementProgressBars(-1, "Create Dir: " + ifsfile.getAbsolutePath());
                                    if (!ifsfile.mkdir()) {
                                        throw new IFSUnpackerException("Error Creating Directory: " + file);
                                    }
                                }
                            }
                            zipstream.closeEntry();
                        }
                        continue;
                    }
                    catch (FileNotFoundException e) {
                        throw new IFSUnpackerException("Error File NOT Found: " + installFile.file.getAbsolutePath(), e);
                    }
                    catch (IOException e) {
                        if (e.getMessage().matches("Access to request was denied.")) {
                            throw new IFSUnpackerException(String.format("Error Creating: %s\nVerify that your account has write access to this directory", file), e);
                        }
                        throw new IFSUnpackerException("Error Reading File: " + installFile.file.getAbsolutePath(), e);
                    }
                    catch (AS400SecurityException e) {
                        throw new IFSUnpackerException("Error Writing File: " + file, (Throwable)e);
                    }
                }
                finally {
                    if (zipstream != null) {
                        try {
                            zipstream.close();
                        }
                        catch (IOException e) {}
                    }
                    zipstream = null;
                }
            }
            BufferedInputStream input = null;
            String FileName = installFile.ifsFile.getAbsolutePath().replaceAll("[\\\\]|[A-Za-z][:][\\\\]", "/");
            this.incrementProgressBars(++count, "Copying: " + FileName);
            try {
                if (installFile.replace != null && !installFile.replace.booleanValue() && new IFSFile(this.connection, FileName).exists()) continue;
                try {
                    input = new BufferedInputStream(new FileInputStream(installFile.file));
                    try(OutputStream output = new BufferedOutputStream((OutputStream)new IFSFileOutputStream(this.connection, FileName, 1252))){
                    len = 0;
                    while ((len = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, len);
                        this.incrementByteCount(len);
                    }
                    output.flush();
                    }
                    continue;
                }
                catch (FileNotFoundException e) {
                    throw new IFSUnpackerException("Error File NOT Found: " + installFile.file.getAbsolutePath(), e);
                }
                catch (IOException e) {
                    if (e.getMessage().matches("Access to request was denied.")) {
                        throw new IFSUnpackerException(String.format("Error Creating: %s\nVerify that your account has write access to this directory", FileName), e);
                    }
                    throw new IFSUnpackerException("Error Reading File: " + installFile.file.getAbsolutePath(), e);
                }
                catch (AS400SecurityException e) {
                    throw new IFSUnpackerException("Error Writing File: " + FileName, (Throwable)e);
                }
            } catch (IOException e) {
            	 throw new IFSUnpackerException("Error Reading File: " + FileName, (Throwable)e);
			}
            finally {
                if (input != null) {
                    try {
                        input.close();
                    }
                    catch (IOException e) {}
                }
                input = null;
            }
        }
        this.incrementByteCount(0);
        this.incrementProgressBars(count, "Complete");
    }

    private void remove() throws IFSUnpackerException {
        long Count = 0;
        Stack<String> files = new Stack<String>();
        for(IFSUnpackerFile installFile : this.files){
            {
                if (installFile.remove == null || installFile.remove.booleanValue()) {
                    if (installFile.isZip.booleanValue()) {
                        ZipInputStream zipstream = null;
                        try {
                            try {
                                zipstream = new ZipInputStream(new FileInputStream(installFile.file));
                                ZipEntry zipentry = null;
                                while ((zipentry = zipstream.getNextEntry()) != null) {
                                    files.push("/" + zipentry.getName());
                                    this.incrementProgressBars(++Count, "Building File List");
                                    zipstream.closeEntry();
                                }
                                break;
                            }
                            catch (FileNotFoundException e) {
                                throw new IFSUnpackerException("Error File NOT Found: " + installFile.file.getAbsolutePath(), e);
                            }
                            catch (IOException e) {
                                throw new IFSUnpackerException("Error Reading File: " + installFile.file.getAbsolutePath(), e);
                            }
                        }
                        finally {
                            if (zipstream != null) {
                                try {
                                    zipstream.close();
                                }
                                catch (IOException e) {}
                            }
                        }
                    }
                    files.push(installFile.ifsFile.getAbsolutePath().replaceAll("[\\\\]|[A-Za-z][:][\\\\]", "/"));
                }
            }
            try {
                Count = 0;
                while (!files.isEmpty()) {
                    IFSFile ifsfile = new IFSFile(this.connection, (String)files.pop());
                    if (ifsfile.delete()) continue;
                    this.incrementProgressBars(++Count, "Deleting: " + ifsfile.getAbsolutePath());
                }
                continue;
            }
            catch (IOException e) {
                System.out.println("Error Deleting IFSFile: " + e.getMessage());
            }
        }
    }
}


