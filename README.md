AmiKo-Desktop
=============

AmiKo Desktop für Windows

##Setup
* The database goes into jars/dbs/
* * amiko_db_full_idx_de.db
* * amiko_db_full_idx_fr.db

###Grab the database from here:
* wget http://pillbox.oddb.org/amiko_db_full_idx_de.db

### Error File:
* http://pillbox.oddb.org/amiko_report_de.html

## Help
* java -jar amikowindows.jar --help

## Version
* java -jar amikowindows.jar --version

## Usage to show one Fachinfo
### Full Window
* java -jar amikowindows.jar --lang=de --type=full --regnr=62069

### Light Window
* java -jar amikowindows.jar --lang=de --type=light --regnr=62069

### Accessing the database via TCP-client
1. Start Server: java -jar amikowindows.jar --lang=de --type=light --port=7777
2. Send requests from client using: t = title, e = eancode, r = regnr

Write your own client (Ruby, Java, PHP, C#, etc). Test client in java is here:
* https://github.com/cybermax/simpleclient
* to start the sample client do: java -jar simpleclient.jar
