# AdClear_CodingEvaluation

-----About--------------
HTTPServer main executes the functionality outlined in the program specification. Upon running HTTPServer,
the server itself starts, the various handlers are initialized, which call the functions within ServerAPI,
which holds the majority of the functionality code.

HTTPClient was created to send requests to the HTTPServer to execute the various tasks outlined in the
program specification. HTTPClient consists of nested while loops and switch statements that act as a 
UI for the user to navigate, making it easier to execute a task. ClientAPI holds the majority of the
functionality code for the client side.

With regards to sending requests to the server, the client has pre-written requests, a valid request and
each type of invalid requets, that can be sent to the server. As well as this the client allows for 
user inputted JSON Strings to be sent as requests.

With regards to receiving hourly statistics from the hourly_stats table, the client allows the user to choose
which user they want to retrieve data on by displaying a list of options, and then allowing the user to input
the specific date that they would like to inquire about.

The database used was PostgreSQL



-----Setting Up and Running--------------
!!!MUST BE RUN IN AN IDE!!!

Clone the repository and import the folder into the IDE (preferably Eclipse)

Databse used Was PostgreSQL, so make changes accordingly; Create a new table in the database called requestLog,
which will temporarily store all counts of valid and invalid requests made by a user within an hour, before being
sent to the hourly_stats table. 

CREATE TABLE requestLog(
customerID int NOT NULL PRIMARY KEY,
request_count bigint check (request_count >= 0) NOT NULL,
invalid_count bigint check (invalid_count >= 0) NOT NULL
);

Run HTTPServer, followed by HTTPClient, and follow the HTTPClient UI in the console log to execute tasks.






