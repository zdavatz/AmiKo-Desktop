#!/bin/bash

# sign all files before...
WINDIR="C:\Program Files (x86)\Windows Kits\8.0\bin\x86"
INNODIR="C:\Program Files (x86)\Inno Setup 5"

EXE1="./01_amikodesk_exe32/amikodesk.exe"
EXE2="./02_comeddesk_exe32/comeddesk.exe"
EXE3="./03_amikodesk_desitin_exe32/amikodeskdesitin.exe"
EXE4="./04_comeddesk_desitin_exe32/comeddeskdesitin.exe"
EXE5="./05_meddrugs_exe32/meddrugsdesk.exe"
EXE6="./06_meddrugs_fr_exe32/meddrugsfrdesk.exe"
EXE7="./07_amikodesk_zurrose_exe32/amikodeskzr.exe"
EXE8="./08_comeddesk_zurrose_exe32/comeddeskzr.exe"

for file in $EXE1 $EXE2 $EXE3 $EXE4 $EXE5 $EXE6 $EXE7 $EXE8; 
do
	"$WINDIR\signtool.exe" \sign "$file"
done

# innosetup
for inno_script in *.iss; 
do
	"$INNODIR\iscc.exe" $inno_script
done

# sign all files after...
EXE1="../output/amikodesk_setup_32bit.exe"
EXE2="../output/comeddesk_setup_32bit.exe"
EXE3="../output/amikodeskdesitin_setup_32bit.exe"
EXE4="../output/comeddeskdesitin_setup_32bit.exe"
EXE5="../output/meddrugs_setup_32bit.exe"
EXE6="../output/meddrugsfr_setup_32bit.exe"
EXE7="../output/amikodeskzr_setup_32bit.exe"
EXE8="../output/comeddeskzr_setup_32bit.exe"

for file in $EXE1 $EXE2 $EXE3 $EXE4 $EXE5 $EXE6 $EXE7 $EXE8; 
do
	"$WINDIR\signtool.exe" \sign "$file"
done


