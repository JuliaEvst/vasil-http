CREATE TABLE USERS (
    USER_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    ADDRESS VARCHAR(255) NOT NULL,
    USER_ROLE VARCHAR(255) NOT NULL,
    PASSWORD INT NOT NULL,
    FOREIGN KEY (USER_ROLE)
    REFERENCES PERMISSIONS(ROLE)
);