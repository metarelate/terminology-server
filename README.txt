HOW TO INSTALL
--------------

To install the terminology server run:
java -jar installer--version

You will be asked for a few questions:

1) The directory where you want the system to be installed.
2) The seed for this terminology server instance (accept default unless you are
replacing an older installation)
3) The URI for the default user (essentially an identifier for a "default" user,
this can be shared across instances)
4) The base url where the terminology should be published at. Note that this
should be consistent with the URIs in your terminology if you want the online
system to resolve files. This information is not used by the -doc publisher
5) The base root on the file-system where to write a web-based representation
of the terminology (only used by the -web publisher)
6) the path to the pdflatex command (not currently used: the system output
latex).

Once you have installed the system you can run it through the command ts.jar
(java -jar ts.jar help), that is in the command folder under the installation
directory. A simple tsc.bat command is provided for convenience (can be run as
"tsc.sh help", if exec permissions are set and the command is in the path).

In order to know where the system directory is, the system proceeds as follows:
1) If a valid directory is provided via the -sys parameter, this is used as the
system directory
2) failing this, the system will look at the content of the TSHOME environment 
variable
3) failing this, the system will look in $HOME/.tserver

Note that (1) is currently not recognized by the web-admin functionalities 
(option 2 is suggested).

The system offers a variety of command whose explanation can be obtained via the
tsc.sh help COMMANDNAME option.
There are for main command:
1) tsc.sh ingest : to import a terminology in the system
2) tsc.sh tag : to tag the current state of a terminology
3) tsc.sh web : opens a web interface for admistration
4) tsc.sh publish : publishes the terminology. This command has three
modalities:
A) -web : publishes the terminology as a set of files, to be served by the 
web server via content negotiation (requires some web-server configuration)
B) -online : opens a server that resolves terminology URLs
C) -doc : output a latex doc for a given tag
The above mechanisms make use of templates that can be provided by the user 
(a few default templates are provided).

Note that the system distinguishes between URIs (concept identifiers) and URLs 
(concepts "locators"), so that a terminology can be published at different
addresses. 
