-- MySQL dump 10.13  Distrib 5.5.47, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: valuesets
-- ------------------------------------------------------
-- Server version	5.5.47-0ubuntu0.14.04.1

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
-- Table structure for table `ValueSet`
--

DROP TABLE IF EXISTS `ValueSet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSet` (
  `name` varchar(255) NOT NULL,
  `formalName` varchar(255) DEFAULT NULL,
  `href` varchar(255) DEFAULT NULL,
  `uri` varchar(255) DEFAULT NULL,
  `currentVersion_documentUri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FKAFCCD55113E71833` (`currentVersion_documentUri`),
  CONSTRAINT `FKAFCCD55113E71833` FOREIGN KEY (`currentVersion_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetChange`
--

DROP TABLE IF EXISTS `ValueSetChange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetChange` (
  `changeSetUri` varchar(255) NOT NULL,
  `closeDate` datetime DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `instructions` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `currentVersion_documentUri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`changeSetUri`),
  KEY `FK6B2DD46113E71833` (`currentVersion_documentUri`),
  CONSTRAINT `FK6B2DD46113E71833` FOREIGN KEY (`currentVersion_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetChange_ValueSetVersion`
--

DROP TABLE IF EXISTS `ValueSetChange_ValueSetVersion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetChange_ValueSetVersion` (
  `ValueSetChange_changeSetUri` varchar(255) NOT NULL,
  `versions_documentUri` varchar(255) NOT NULL,
  UNIQUE KEY `versions_documentUri` (`versions_documentUri`),
  KEY `FK45FABA69E7D3BF3E` (`ValueSetChange_changeSetUri`),
  KEY `FK45FABA69B8AD778F` (`versions_documentUri`),
  CONSTRAINT `FK45FABA69B8AD778F` FOREIGN KEY (`versions_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`),
  CONSTRAINT `FK45FABA69E7D3BF3E` FOREIGN KEY (`ValueSetChange_changeSetUri`) REFERENCES `ValueSetChange` (`changeSetUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetEntry`
--

DROP TABLE IF EXISTS `ValueSetEntry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetEntry` (
  `id` varchar(255) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `codeSystem` varchar(255) DEFAULT NULL,
  `codeSystemVersion` varchar(255) DEFAULT NULL,
  `description` longtext,
  `valueSetVersion_documentUri` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5628EDA1EF4EED5B` (`valueSetVersion_documentUri`),
  CONSTRAINT `FK5628EDA1EF4EED5B` FOREIGN KEY (`valueSetVersion_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetProperty`
--

DROP TABLE IF EXISTS `ValueSetProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetProperty` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `value` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetProperty_qualifiers`
--

DROP TABLE IF EXISTS `ValueSetProperty_qualifiers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetProperty_qualifiers` (
  `ValueSetProperty_id` varchar(255) NOT NULL,
  `qualName` varchar(255) DEFAULT NULL,
  `qualValue` varchar(255) DEFAULT NULL,
  KEY `FK2B9CBCA29A1C78FF` (`ValueSetProperty_id`),
  CONSTRAINT `FK2B9CBCA29A1C78FF` FOREIGN KEY (`ValueSetProperty_id`) REFERENCES `ValueSetProperty` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetVersion`
--

DROP TABLE IF EXISTS `ValueSetVersion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetVersion` (
  `documentUri` varchar(255) NOT NULL,
  `binding` varchar(255) DEFAULT NULL,
  `changeCommitted` varchar(255) DEFAULT NULL,
  `changeSetUri` varchar(255) DEFAULT NULL,
  `changeType` varchar(255) DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `prevChangeSetUri` varchar(255) DEFAULT NULL,
  `qdmCategory` varchar(255) DEFAULT NULL,
  `revisionDate` datetime DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `state` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `successor` varchar(255) DEFAULT NULL,
  `synopsis` varchar(255) DEFAULT NULL,
  `valueSetType` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `valueSet_name` varchar(255) NOT NULL,
  PRIMARY KEY (`documentUri`),
  KEY `FKE3767247469B2FEF` (`valueSet_name`),
  CONSTRAINT `FKE3767247469B2FEF` FOREIGN KEY (`valueSet_name`) REFERENCES `ValueSet` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSetVersion_includesValueSets`
--

DROP TABLE IF EXISTS `ValueSetVersion_includesValueSets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSetVersion_includesValueSets` (
  `ValueSetVersion_documentUri` varchar(255) NOT NULL,
  `includesValueSets` varchar(255) DEFAULT NULL,
  KEY `FKE1F340DFEF4EED5B` (`ValueSetVersion_documentUri`),
  CONSTRAINT `FKE1F340DFEF4EED5B` FOREIGN KEY (`ValueSetVersion_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSet_ValueSetProperty`
--

DROP TABLE IF EXISTS `ValueSet_ValueSetProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSet_ValueSetProperty` (
  `ValueSet_name` varchar(255) NOT NULL,
  `properties_id` varchar(255) NOT NULL,
  UNIQUE KEY `properties_id` (`properties_id`),
  KEY `FKAA80EF749EE5AD2` (`properties_id`),
  KEY `FKAA80EF74469B2FEF` (`ValueSet_name`),
  CONSTRAINT `FKAA80EF74469B2FEF` FOREIGN KEY (`ValueSet_name`) REFERENCES `ValueSet` (`name`),
  CONSTRAINT `FKAA80EF749EE5AD2` FOREIGN KEY (`properties_id`) REFERENCES `ValueSetProperty` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueSet_ValueSetVersion`
--

DROP TABLE IF EXISTS `ValueSet_ValueSetVersion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueSet_ValueSetVersion` (
  `ValueSet_name` varchar(255) NOT NULL,
  `versions_documentUri` varchar(255) NOT NULL,
  UNIQUE KEY `versions_documentUri` (`versions_documentUri`),
  KEY `FKBD9DB59469B2FEF` (`ValueSet_name`),
  KEY `FKBD9DB59B8AD778F` (`versions_documentUri`),
  CONSTRAINT `FKBD9DB59469B2FEF` FOREIGN KEY (`ValueSet_name`) REFERENCES `ValueSet` (`name`),
  CONSTRAINT `FKBD9DB59B8AD778F` FOREIGN KEY (`versions_documentUri`) REFERENCES `ValueSetVersion` (`documentUri`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-12  5:13:36
