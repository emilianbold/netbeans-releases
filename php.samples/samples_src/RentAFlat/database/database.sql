-- MySQL dump 10.13  Distrib 5.1.49, for apple-darwin10.3.0 (i386)
--
-- Host: localhost    Database: rentaflat
-- ------------------------------------------------------
-- Server version	5.1.49

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `disposition`
--

DROP TABLE IF EXISTS `disposition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `disposition` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text_en` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disposition`
--

LOCK TABLES `disposition` WRITE;
/*!40000 ALTER TABLE `disposition` DISABLE KEYS */;
INSERT INTO `disposition` VALUES (1,'1+kk'),(2,'1+1'),(3,'2+kk'),(4,'2+1'),(5,'3+kk'),(6,'3+1'),(21,'Apartment house'),(12,'4+1'),(13,'4+kk'),(14,'5+1'),(15,'5+kk'),(16,'6+1'),(17,'6+kk'),(26,'1+0');
/*!40000 ALTER TABLE `disposition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `city_part` text,
  `city` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'Chodov','Prague');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reference_no` varchar(255) DEFAULT NULL,
  `title_en` text,
  `text_en` text,
  `disposition_id` int(4) DEFAULT NULL,
  `area` float(10,1) DEFAULT NULL,
  `floor` int(4) DEFAULT NULL,
  `lift` int(1) DEFAULT NULL,
  `cellar` float(10,1) DEFAULT NULL,
  `balcony` float(10,1) DEFAULT NULL,
  `location_id` int(4) DEFAULT NULL,
  `price` int(10) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `street` text,
  `property_build_id` int(11) DEFAULT NULL,
  `terace` float(10,1) DEFAULT NULL,
  `loggia` float(10,1) DEFAULT NULL,
  `garden` float(10,1) DEFAULT NULL,
  `garage` float(10,1) DEFAULT NULL,
  `parking_place` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `reference_no` (`reference_no`)
) ENGINE=MyISAM AUTO_INCREMENT=609 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property`
--

LOCK TABLES `property` WRITE;
/*!40000 ALTER TABLE `property` DISABLE KEYS */;
INSERT INTO `property` VALUES (66,'NetBeans001','Great flat for PHP Development','This fabulous three bedroomed penthouse apartment is arranged over the sixth and seventh floors (with lift and concierge) of Orechovka, a smart residential development on the banks of the River Vltava.  The property offers chic and spacious accommodation and comprises vast reception room with door to the roof terrace, contemporary kitchen with ample space for dining, master bedroom with en suite shower room, two additional bedrooms, bathroom, good storage space, balcony, roof terrace, and secure underground parking.',8,110.0,2,1,0.0,40.0,1,1000,'2010-11-03 11:15:44','V parku 2308/8',1,0.0,0.0,0.0,0.0,1),(64,'NetBeans002','Great flat for J2EE Development','Benefiting from well proportioned living space and high-quality interiors throughout, this recently refurbished two bedroomed flat is attractively arranged over the ground and lower ground floors of a beautiful period block in the heart of historic Marylebone.  The property comprises reception room with well equipped contemporary kitchen, master bedroom with en suite shower room, second bedroom/study, bathroom and guest cloakroom. ',6,99.2,3,0,0.0,0.0,1,1500,'2010-11-08 15:32:51','V parku 2308/8',1,0.0,0.0,0.0,0.0,0),(60,'NetBeans003','Great flat for Java Web Development','A simply stunning two bedroomed third floor flat offering generous accommodation, with stylish and neutral décor, fantastic roof terrace and gorgeous wood floors.  The property comprises spacious reception room with lovely gas fireplace, amazing adjoining kitchen, contemporary master bedroom with chic en suite bathroom, second bedroom with fitted wardrobes, en suite shower room and guest cloakroom.',5,98.6,4,1,0.0,5.5,1,2000,'2010-11-01 08:40:08','V parku 2308/8',1,0.0,0.0,0.0,0.0,0),(54,'NetBeans004','Great flat for Javascript Development','With a sought-after location in the heart of Belsize Park, this stunning three bedroomed maisonette offers abundant living space with modern fixtures and fittings, wood floors and en suite master bedroom.  Situated on the first and second floors of a grand period conversion the property comprises a light and spacious reception room with open-plan kitchen, dining room, utility room, excellent master bedroom with en suite bathroom, two further good-sized bedrooms and shower room.',6,76.0,5,1,0.0,0.0,1,800,'2010-11-08 12:34:09','V parku 2308/8',2,0.0,0.0,0.0,0.0,0),(604,'NetBeans005','Apartment for you and entire family','This light and airy, three bedroomed, first floor flat (with lift and porterage) is exceptionally proportioned throughout benefiting from a secure gated mews with security guard, sunny terrace and sought-after location.  The property comprises spacious reception room with access to balcony, fully fitted kitchen, master bedroom with en suite bathroom, second bedroom with fitted wardrobe, one additional bedroom and shower room.',2,324.0,6,0,0.0,34.0,1,950,'2010-12-29 01:28:24','V parku 2308/8',1,4.0,2.0,0.0,32.0,0),(605,'NetBeans006','Flat suitable for students','Benefiting from well proportioned living space and high-quality interiors throughout, this recently refurbished two bedroomed flat is attractively arranged over the ground and lower ground floors of a beautiful period block close to Czech Technical University complex.  The property comprises reception room with well equipped contemporary kitchen, master bedroom with en suite shower room, second bedroom/study, bathroom and guest cloakroom.',5,800.0,1,0,0.0,0.0,1,2210,'2011-01-02 00:45:21','V parku 2308/8',1,0.0,0.0,0.0,0.0,0),(606,'NetBeans007','2 double bedroom apartment','We are pleased to offer for rental this executive apartment situated within the Cathedral complex in city center. There is an open plan living area two double bedrooms and allocated parking space. A viewing is highly recommended.',2,51.0,19,0,0.0,0.0,1,1209,'2011-01-02 22:23:06','V parku 2308/8',1,0.0,0.0,0.0,0.0,0),(607,'NetBeans008','Renovated apartment + parking place','Completely re-decorated ground floor apartment in the popular \'Mezzo\' complex in Prague. This lovely apartment offers 1 double and 1 generous single bedroom, family bathroom with over bath shower and a large open plan living / dining / kitchen with high specification integrated appliances and contemporary fixtures and fittings. With a security entrance system, allocated parking and a rarely available private low maintenance rear garden with decking, this is a lovely property in a great location with easy access to the city centre,',6,51.0,0,1,23.0,34.0,1,1400,'2011-01-03 16:40:16','V parku 2308/8',1,4.0,2.0,23.0,32.0,1),(608,'NetBeans009','4 bed flat to rent','A gorgeous four bedroomed flat offering an extremely high standard of interior styling and design, African wood flooring, professional soundproofing, modern fittings and fixtures, gorgeous bathroom facilities and plenty of living and entertaining space.\r\n\r\nThe property further benefits from original period features and recent refurbishment to a very high standard.\r\n\r\nThe flat comprises spacious 30\\\\\\\' double reception room with dining area and access to the balcony, modern fitted kitchen with dining area, bay-fronted master bedroom, two further good-sized bedrooms, study/fourth bedroom, bathroom, shower room, guest cloakroom and a balcony.',14,150.0,3,1,0.0,30.0,1,3000,'2011-01-04 21:53:22','V parku 2308/8',1,0.0,0.0,0.0,0.0,1);
/*!40000 ALTER TABLE `property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_build`
--

DROP TABLE IF EXISTS `property_build`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text_en` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_build`
--

LOCK TABLES `property_build` WRITE;
/*!40000 ALTER TABLE `property_build` DISABLE KEYS */;
INSERT INTO `property_build` VALUES (1,'brick'),(2,'concrete'),(3,'skeletal structure');
/*!40000 ALTER TABLE `property_build` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-01-04 21:58:01
