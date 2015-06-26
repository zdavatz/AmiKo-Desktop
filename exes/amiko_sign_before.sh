#!/bin/bash

WINDIR="C:\Program Files (x86)\Windows Kits\8.0\bin\x86"

EXE1a="./01_amikodesk_exe32/amikodesk.exe"
EXE1b="./02_comeddesk_exe32/comeddesk.exe"
EXE2a="./03_amikodesk_desitin_exe32/amikodeskdesitin.exe"
EXE2b="./04_comeddesk_desitin_exe32/comeddeskdesitin.exe"
EXE3a="./05_meddrugs_exe32/meddrugsdesk.exe"
EXE3b="./06_meddrugs_fr_exe32/meddrugsfrdesk.exe"
EXE4a="./07_amikodesk_zurrose_exe32/amikodeskzr.exe"
EXE4b="./08_comeddesk_zurrose_exe32/comeddeskzr.exe"
EXE5a="./09_amikodesk_ibsa_exe32/amikodeskibsa.exe"
EXE5b="./10_comeddesk_ibsa_exe32/comeddeskibsa.exe"

for file in $EXE1a $EXE1b $EXE2a $EXE2b $EXE3a $EXE3b $EXE4a $EXE4b $EXE5a $EXE5b; 
do
"$WINDIR\signtool.exe" \sign "$file"
done


