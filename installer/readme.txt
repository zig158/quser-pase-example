# Create link to usr/bin to add ghostscript to the system path
ln -s /usr/local/share/ghostscript/9.05/bin/gs /usr/bin/gs

# Convert pdf document to PCL
gs -dSAFER -dBATCH -dNOPAUSE -sDEVICE=PXLMONO -sOutputFile=sample.pcl sample.pdf