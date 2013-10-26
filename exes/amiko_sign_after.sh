#!/bin/bash

WINDIR="C:\Program Files (x86)\Windows Kits\8.0\bin\x86"

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


