# Cafe Database Project

Created by: Justin Mac and Jonathan Tan

How to start the Cafe program:

First time run (only have to do this once):
The computer you are using must be Postgre compatible with the database name = mydb. If you have not initialized the database on the computer, use the command 'cs166_initdb'. You only have to initialize the database once.  Next, input the command 'createdb -h 127.0.0.1 mydb' to create the database with the name mydb. To load the data into the database, open project/sql/src/load_data.sql and change the path to where the data files are. (i.e. /extra/jmac006/cs166/project/data/menu.csv) Important: Make sure to use absolute paths to avoid ambiguity. After changing the paths of the files, change the directory to project/sql/scripts and run 'create_db.sh' to load the database. 

To run the program:
First, stop the current postgres server by using the command 'cs166_db_stop' in case you did not stop the server from the last session. Start the database by using 'cs166_db_start'. Change the directory to project/java/scripts/compile.sh in order to run the Cafe program. 

About the program:
This application is built for a Cafe allowing users to see real-time changes on their orders. This program supports creating new users, logging users in, browsing the menu, adding orders, updating menu orders, viewing order statuses, and updating user information.

How we designed the database:
blah.

What still needs to be implemented:
  - EmployeeUpdateOrder
  - UpdateUserInfo
  - ManagerUpdateUserInfo
  - UpdateMenu
  - Validation checks (Make application more user friendly)
  - Anything extra

