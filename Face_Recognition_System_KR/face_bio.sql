


--
-- Table structure for table `face_bio`
--

CREATE TABLE face_bio (
	`id` int(11) NOT NULL AUTO_INCREMENT,
  `code` int(10) NOT NULL,
  `first_name` varchar(30) NOT NULL,
  `last_name` varchar(20) NOT NULL,
  `age` int(10) NOT NULL,
    PRIMARY KEY (`id`)
);


--
-- Table structure for table `face_bio_photo`
--

CREATE TABLE `face_bio_photo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `image` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=latin1;
