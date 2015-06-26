#!/bin/bash

WINDIR="C:\Program Files (x86)\Windows Kits\8.0\bin\x86"

EXE1a="../output/amikodesk_setup_32bit.exe"
EXE1b="../output/comeddesk_setup_32bit.exe"
EXE2a="../output/amikodeskdesitin_setup_32bit.exe"
EXE2b="../output/comeddeskdesitin_setup_32bit.exe"
EXE3a="../output/meddrugs_setup_32bit.exe"
EXE3b="../output/meddrugsfr_setup_32bit.exe"
EXE4a="../output/amikodeskzr_setup_32bit.exe"
EXE4b="../output/comeddeskzr_setup_32bit.exe"
EXE5a="../output/amikodeskibsa_setup_32bit.exe"
EXE5b="../output/comeddeskibsa_setup_32bit.exe"

for file in $EXE1a $EXE1b $EXE2a $EXE2b $EXE3a $EXE3b $EXE4a $EXE4b $EXE5a $EXE5b; 
do
"$WINDIR\signtool.exe" \sign "$file"
done


