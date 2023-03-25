CREATE TABLE CATEGORY (
    CATEGORY_NAME VARCHAR(255) PRIMARY KEY,
    PARENT_CATEGORY_NAME VARCHAR(255) NOT NULL,
    FOREIGN KEY (PARENT_CATEGORY_NAME)
    REFERENCES PARENT_CATEGORY(PARENT_CATEGORY_NAME) ON UPDATE CASCADE ON DELETE CASCADE
);