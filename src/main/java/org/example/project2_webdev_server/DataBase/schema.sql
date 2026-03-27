CREATE DATABASE project2;

USE project2;

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       profile_image_url VARCHAR(255),
                       token VARCHAR(255)
);

CREATE TABLE follows (
                         follower_username VARCHAR(50) NOT NULL,
                         followed_username VARCHAR(50) NOT NULL,

                         PRIMARY KEY (follower_username, followed_username),

                         FOREIGN KEY (follower_username)
                             REFERENCES users(username)
                             ON DELETE CASCADE,

                         FOREIGN KEY (followed_username)
                             REFERENCES users(username)
                             ON DELETE CASCADE
);


CREATE TABLE posts (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       author_username VARCHAR(50) NOT NULL,
                       content TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       FOREIGN KEY (author_username)
                           REFERENCES users(username)
                           ON DELETE CASCADE
);
