
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT=0;
START TRANSACTION;

-- --------------------------------------------------------

CREATE TABLE `disposition` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=15 ;

INSERT INTO `disposition` (`id`, `text`) VALUES
(1, '1+0'),
(2, '1+kk'),
(3, '1+1'),
(4, '2+kk'),
(5, '2+1'),
(6, '3+kk'),
(7, '3+1'),
(8, '4+1'),
(9, '4+kk'),
(10, '5+1'),
(11, '5+kk'),
(12, '6+1'),
(13, '6+kk'),
(14, 'Apartment house');

-- --------------------------------------------------------

CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `city_part` text,
  `city` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=3 ;

INSERT INTO `location` (`id`, `city_part`, `city`) VALUES
(1, 'Chodov', 'Prague'),
(2, 'Vinohrady', 'Prague');

-- --------------------------------------------------------

CREATE TABLE `property` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reference_no` varchar(255) NOT NULL,
  `title` text NOT NULL,
  `text` text NOT NULL,
  `disposition_id` int(11) NOT NULL,
  `area` float(10,2) NOT NULL,
  `floor` int(11) NOT NULL,
  `lift` tinyint(1) NOT NULL,
  `cellar` float(10,2) DEFAULT NULL,
  `balcony` float(10,2) DEFAULT NULL,
  `location_id` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `created_on` datetime NOT NULL,
  `street` text NOT NULL,
  `property_build_id` int(11) NOT NULL,
  `terace` float(10,2) DEFAULT NULL,
  `loggia` float(10,2) DEFAULT NULL,
  `garden` float(10,2) DEFAULT NULL,
  `garage` float(10,2) DEFAULT NULL,
  `parking_place` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `disposition_id_idx` (`disposition_id`),
  KEY `location_id_idx` (`location_id`),
  KEY `property_build_id_idx` (`property_build_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

INSERT INTO `property` (`id`, `reference_no`, `title`, `text`, `disposition_id`, `area`, `floor`, `lift`, `cellar`, `balcony`, `location_id`, `price`, `created_on`, `street`, `property_build_id`, `terace`, `loggia`, `garden`, `garage`, `parking_place`) VALUES
(1, 'NetBeans001', 'Great flat for PHP Development', 'This fabulous three bedroomed penthouse apartment is arranged over the sixth and seventh floors (with lift and concierge) of Orechovka, a smart residential development on the banks of the River Vltava.  The property offers chic and spacious accommodation and comprises vast reception room with door to the roof terrace, contemporary kitchen with ample space for dining, master bedroom with en suite shower room, two additional bedrooms, bathroom, good storage space, balcony, roof terrace, and secure underground parking.', 11, 110.00, 2, 1, 0.00, 40.00, 1, 1000, '2010-11-03 11:15:44', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 1),
(2, 'NetBeans002', 'Great flat for J2EE Development', 'Benefiting from well proportioned living space and high-quality interiors throughout, this recently refurbished two bedroomed flat is attractively arranged over the ground and lower ground floors of a beautiful period block in the heart of historic Marylebone.  The property comprises reception room with well equipped contemporary kitchen, master bedroom with en suite shower room, second bedroom/study, bathroom and guest cloakroom. ', 7, 99.20, 3, 0, 0.00, 0.00, 1, 1500, '2010-11-08 15:32:51', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 0),
(3, 'NetBeans003', 'Great flat for Java Web Development', 'A simply stunning two bedroomed third floor flat offering generous accommodation, with stylish and neutral décor, fantastic roof terrace and gorgeous wood floors.  The property comprises spacious reception room with lovely gas fireplace, amazing adjoining kitchen, contemporary master bedroom with chic en suite bathroom, second bedroom with fitted wardrobes, en suite shower room and guest cloakroom.', 6, 98.60, 4, 1, 0.00, 5.50, 1, 2000, '2010-11-01 08:40:08', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 0),
(4, 'NetBeans004', 'Great flat for Javascript Development', 'With a sought-after location in the heart of Belsize Park, this stunning three bedroomed maisonette offers abundant living space with modern fixtures and fittings, wood floors and en suite master bedroom.  Situated on the first and second floors of a grand period conversion the property comprises a light and spacious reception room with open-plan kitchen, dining room, utility room, excellent master bedroom with en suite bathroom, two further good-sized bedrooms and shower room.', 7, 76.00, 5, 1, 0.00, 0.00, 2, 800, '2010-11-08 12:34:09', 'V parku 2308/8', 2, 0.00, 0.00, 0.00, 0.00, 0),
(5, 'NetBeans005', 'Apartment for you and entire family', 'This light and airy, three bedroomed, first floor flat (with lift and porterage) is exceptionally proportioned throughout benefiting from a secure gated mews with security guard, sunny terrace and sought-after location.  The property comprises spacious reception room with access to balcony, fully fitted kitchen, master bedroom with en suite bathroom, second bedroom with fitted wardrobe, one additional bedroom and shower room.', 14, 324.00, 6, 0, 0.00, 34.00, 1, 950, '2010-12-29 01:28:24', 'V parku 2308/8', 1, 4.00, 2.00, 0.00, 32.00, 0),
(6, 'NetBeans006', 'Flat suitable for students', 'Benefiting from well proportioned living space and high-quality interiors throughout, this recently refurbished two bedroomed flat is attractively arranged over the ground and lower ground floors of a beautiful period block close to Czech Technical University complex.  The property comprises reception room with well equipped contemporary kitchen, master bedroom with en suite shower room, second bedroom/study, bathroom and guest cloakroom.', 6, 800.00, 1, 0, 0.00, 0.00, 1, 2210, '2011-01-02 00:45:21', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 0),
(7, 'NetBeans007', '2 double bedroom apartment', 'We are pleased to offer for rental this executive apartment situated within the Cathedral complex in city center. There is an open plan living area two double bedrooms and allocated parking space. A viewing is highly recommended.', 3, 51.00, 19, 0, 0.00, 0.00, 1, 1209, '2011-01-02 22:23:06', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 0),
(8, 'NetBeans008', 'Renovated apartment + parking place', 'Completely re-decorated ground floor apartment in the popular ''Mezzo'' complex in Prague. This lovely apartment offers 1 double and 1 generous single bedroom, family bathroom with over bath shower and a large open plan living / dining / kitchen with high specification integrated appliances and contemporary fixtures and fittings. With a security entrance system, allocated parking and a rarely available private low maintenance rear garden with decking, this is a lovely property in a great location with easy access to the city centre,', 7, 51.00, 0, 1, 23.00, 34.00, 1, 1400, '2011-01-03 16:40:16', 'V parku 2308/8', 1, 4.00, 2.00, 23.00, 32.00, 1),
(9, 'NetBeans009', '4 bed flat to rent', 'A gorgeous four bedroomed flat offering an extremely high standard of interior styling and design, African wood flooring, professional soundproofing, modern fittings and fixtures, gorgeous bathroom facilities and plenty of living and entertaining space.\r\n\r\nThe property further benefits from original period features and recent refurbishment to a very high standard.\r\n\r\nThe flat comprises spacious 30\\\\\\'' double reception room with dining area and access to the balcony, modern fitted kitchen with dining area, bay-fronted master bedroom, two further good-sized bedrooms, study/fourth bedroom, bathroom, shower room, guest cloakroom and a balcony.', 10, 150.00, 3, 1, 0.00, 30.00, 2, 3000, '2011-01-04 21:53:22', 'V parku 2308/8', 1, 0.00, 0.00, 0.00, 0.00, 1);

-- --------------------------------------------------------

CREATE TABLE `property_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=4 ;

INSERT INTO `property_build` (`id`, `text`) VALUES
(1, 'brick'),
(2, 'concrete'),
(3, 'skeletal structure');

ALTER TABLE `property`
  ADD CONSTRAINT `property_disposition_id_disposition_id` FOREIGN KEY (`disposition_id`) REFERENCES `disposition` (`id`),
  ADD CONSTRAINT `property_location_id_location_id` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  ADD CONSTRAINT `property_property_build_id_property_build_id` FOREIGN KEY (`property_build_id`) REFERENCES `property_build` (`id`);
COMMIT;
