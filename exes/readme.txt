1. In File "AmiKoDesk.java":
	select APP_NAME 
	change VERSION 
	change GEN_DATE
2. In Folder /libs swap Libraries: swt -> swt_64, swt_32 -> swt 
3. File -> Export -> Runnable Jar File (make sure to save in correct folder!)
4. Sign jar file by running ./amiko_sign_before.sh
5. Update Version number in InnoSetup Script and COMPILE (Ctrl + F9)
6. Sign exe file by running ./amiko_sign_after.sh

