-- Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
-- 
-- * Redistributions of source code must retain the above copyright notice,
--   this list of conditions and the following disclaimer.
-- 
-- * Redistributions in binary form must reproduce the above copyright notice,
--   this list of conditions and the following disclaimer in the documentation
--   and/or other materials provided with the distribution.
--
-- * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
--   may be used to endorse or promote products derived from this software without
--   specific prior written permission.
-- 
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
-- ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
-- LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
-- CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
-- SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
-- INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
-- CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
-- ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
-- THE POSSIBILITY OF SUCH DAMAGE.

-- Travel database (assumes you already have a travel schema)
-- create a database connection in the Services tab with the following format
-- for the Database URL	jdbc:derby://localhost:1527/travel;create=true
-- then open this file from the File menu and choose the connection above to execute
-- create tables


drop view  travel.PersonTrip;
drop table travel.Flight;
drop table travel.CarRental;
drop table travel.Hotel;
drop table travel.Trip;
drop table travel.Person;
drop table travel.TripType;
drop table travel.validation_table;

create table travel.TripType (
  TripTypeID                  INTEGER NOT NULL,
  Name                        VARCHAR(15),
  Description                 VARCHAR(50),
  LastUpdated                 TIMESTAMP
);

alter table travel.TripType
  add constraint travel_TripTypePK
  PRIMARY KEY (TripTypeID);

insert into travel.TripType values( 1, 'TRNG', 'Training', NULL); 
insert into travel.TripType values( 2, 'SALES', 'Sales', NULL); 
insert into travel.TripType values( 3, 'OTHER', 'Other', NULL); 
insert into travel.TripType values( 4, 'PR/AR', 'Press and Analyst Meeting', NULL); 
insert into travel.TripType values( 5, 'OFFSITE', 'Offsite Meeting', NULL); 
insert into travel.TripType values( 6, 'CONF', 'Conference/Tradeshow', NULL); 
insert into travel.TripType values( 7, 'REM MTG', 'Remote Office Meeting', NULL); 
insert into travel.TripType values( 8, 'CUST VST', 'Customer Visit', NULL); 
insert into travel.TripType values( 9, 'RECRUIT', 'Recruiting', NULL); 
insert into travel.TripType values( 10, 'BUS DEV', 'Business Development', NULL); 

create table travel.Person (
  PersonID                    INTEGER NOT NULL,
  Name                        VARCHAR(50),
  JobTitle                    VARCHAR(50),
  FrequentFlyer               SMALLINT,
  LastUpdated                 TIMESTAMP
);

alter table travel.Person
  add constraint travel_PersonPK
  PRIMARY KEY (PersonID);

insert into travel.Person values (1, 'Able, Tony', 'CEO', 1, NULL) ;
insert into travel.Person values (2, 'Black, John', 'CTO', 1, NULL) ;
insert into travel.Person values (3, 'Kent, Richard', 'VP', 1, NULL) ;
insert into travel.Person values (4, 'Chen, Larry','VP/CXO - SGMS', 0, NULL) ;
insert into travel.Person values (5, 'Donaldson, Sue', 'VP', 0, NULL) ;     
insert into travel.Person values (6, 'Murrell, Tony', 'VP - SFE', 0, NULL) ;     

create table travel.Trip (
  TripID                      INTEGER NOT NULL,
  PersonID                    INTEGER NOT NULL,
  DepDate                     DATE,
  DepCity                     VARCHAR(32),
  DestCity                    VARCHAR(32),
  TripTypeID                  INTEGER NOT NULL,
  LastUpdated                 TIMESTAMP
);

alter table travel.Trip
  add constraint travel_TripPK
  PRIMARY KEY (TripID);

alter table travel.Trip
  add constraint travel_TripPersonFK
  FOREIGN KEY (PersonID) REFERENCES travel.Person (PersonID);

alter table travel.Trip
  add constraint travel_TripTypeFK
  FOREIGN KEY (TripTypeID) REFERENCES travel.TripType (TripTypeID);
                
insert into travel.Trip values (128, 1, '2008-06-16', 'Oakland', 'New York', 4, NULL) ;
insert into travel.Trip values (199, 1, '2008-09-14', 'San Francisco', 'New York', 4, NULL) ;
insert into travel.Trip values (202, 1, '2008-10-22', 'Oakland', 'Toronto', 4, NULL) ;
insert into travel.Trip values (203, 1, '2008-11-23', 'San Francisco', 'Tokyo', 5, NULL) ;
insert into travel.Trip values (367, 1, '2008-12-12', 'San Francisco', 'Chicago', 2, NULL) ;

insert into travel.Trip values (200, 4, '2008-06-11', 'San Francisco', 'Washington DC', 3, NULL) ;
insert into travel.Trip values (310, 4, '2008-08-03', 'San Jose', 'Washington DC', 3, NULL) ;
insert into travel.Trip values (333, 4, '2009-02-02', 'San Francisco', 'Tokyo', 5, NULL) ;
insert into travel.Trip values (422, 4, '2009-04-11', 'San Jose', 'Washington DC', 3, NULL) ;
insert into travel.Trip values (455, 4, '2009-05-13', 'San Francisco', 'Stockholm', 8, NULL) ;

insert into travel.Trip values (592, 3, '2008-06-16', 'San Jose', 'Novosibirsk', 10, NULL) ;
insert into travel.Trip values (201, 3, '2008-07-01', 'San Jose', 'Washington DC', 8, NULL) ;
insert into travel.Trip values (590, 3, '2008-08-11', 'San Jose', 'Orlando', 6, NULL) ;
insert into travel.Trip values (380, 3, '2009-10-23', 'San Jose', 'Washington DC', 3, NULL) ;
insert into travel.Trip values (421, 3, '2009-11-09',  null, 'Washington DC', 4, NULL) ;

insert into travel.Trip values (100, 2, '2008-05-01', 'Aspen', 'San Francisco', 7, NULL) ;
insert into travel.Trip values (159, 2, '2008-09-01', 'Aspen','Park City', 4, NULL) ;
insert into travel.Trip values (252, 2, '2008-11-01', 'Vail','Chicago', 4, NULL) ;
insert into travel.Trip values (359, 2, '2009-01-26',  null,'Los Angeles', 4, NULL) ;
insert into travel.Trip values (460, 2, '2009-05-06', 'Aspen', 'San Francisco', 4, NULL) ;
insert into travel.Trip values (463, 2, '2009-05-26', 'Glenwood Springs', 'Los Angeles', 6, NULL) ;

insert into travel.Trip values (198, 5, '2008-06-11', 'San Jose', 'Grenoble', 3, NULL) ;
insert into travel.Trip values (208, 5, '2008-06-21', 'San Jose',  'Washington DC', 2, NULL) ;
insert into travel.Trip values (383, 5, '2009-10-23', 'San Jose', 'Grenoble', 3, NULL) ;
insert into travel.Trip values (420, 5, '2009-06-11',  null, 'Philadelphia', 8, NULL) ;

create table travel.Flight (
  TripID                      INTEGER NOT NULL,
  FlightID                    INTEGER NOT NULL,
  Direction                   CHAR(1) NOT NULL,
  FlightNum                   VARCHAR(20),
  DepTime                     TIMESTAMP,
  DepAirport                  VARCHAR(35),
  ArrTime                     TIMESTAMP,
  ArrAirport                  VARCHAR(35),
  AirlineName                 VARCHAR(35),
  BookingStatus               VARCHAR(20),
  LastUpdated                 TIMESTAMP
);

alter table travel.Flight
  add constraint travel_FlightPK
  PRIMARY KEY (FlightID);

alter table travel.Flight
  add constraint travel_FlightFK
  FOREIGN KEY (TripID) REFERENCES travel.Trip (TripID)
  on delete cascade;

-- Flights
insert into travel.Flight values (128, 1, 'D', 'United Airlines 71', '2008-06-16 13:00:00.00', 'Oakland (OAK)', '2008-06-16 22:30:00.00', 'New York Newark Intl Arpt (EWR)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (128, 2, 'R', 'United Airlines 73', '2008-06-23 13:00:00.00', 'New York Newark Intl Arpt (EWR)', '2008-06-23 23:10:00.00', 'Oakland (OAK)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (199, 3, 'D', 'United Airlines 71', '2008-09-14 13:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-09-14 22:30:00.00', 'New York Newark Intl Arpt (EWR)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (199, 4, 'R', 'United Airlines 73', '2008-09-17 13:00:00.00', 'New York Newark Intl Arpt (EWR)', '2008-09-17 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (202, 5, 'D',  'Air Canada 101', '2008-10-22 10:00:00.00', 'Oakland (OAK)', '2008-10-22 23:30:00.00', 'Toronto Intl Arpt (YYZ)', 'Air Canada', 'Confirmed', NULL);
insert into travel.Flight values (202, 6, 'R',  'Air Canada 220', '2008-10-24 13:00:00.00', 'Toronto Intl Arpt (YYZ)', '2008-10-24 17:10:00.00', 'Oakland (OAK)', 'Air Canada', 'Confirmed', NULL);
insert into travel.Flight values (203, 7, 'D', 'Japan Airlines 22', '2008-10-23 13:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-10-23 22:30:00.00', 'Tokyo Narita Intl Arpt (NRT)', 'Japan Airlines', 'Confirmed', NULL);
insert into travel.Flight values (203, 8, 'R', 'Japan Airlines 01', '2008-10-30 13:00:00.00', 'Tokyo Narita Intl Arpt (NRT)', '2008-10-30 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'Japan Airlines', 'Confirmed', NULL);
insert into travel.Flight values (367, 9, 'D', 'United Airlines 62', '2008-12-12 09:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-12-12 17:30:00.00', 'Chicago Ohare Intl Arpt (ORD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (367, 10, 'R', 'United Airlines 32', '2008-12-19 17:00:00.00', 'Chicago Ohare Intl Arpt (ORD)', '2008-12-19 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'United Airlines', 'Confirmed', NULL);

insert into travel.Flight values (200, 11, 'D', 'United Airlines 101', '2008-06-11 09:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-06-11 17:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (200, 12, 'R', 'United Airlines 121', '2008-06-18 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2008-06-18 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (310, 13, 'D', 'American Airlines 10', '2008-08-03 07:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-08-03 18:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'American Airlines', 'Confirmed', NULL);
insert into travel.Flight values (310, 14, 'R', 'American Airlines 12', '2008-08-08 14:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2008-08-08 23:10:00.00', 'San Jose Intl Arpt (SJC)', 'American Airlines', 'Confirmed', NULL);
insert into travel.Flight values (333, 15, 'D', 'Japan Airlines 33', '2009-02-02 09:00:00.00', 'San Francisco Intl Arpt (SFO)', '2009-02-03 17:30:00.00', 'Tokyo Narita Intl Arpt (NRT)','Japan Airlines', 'Confirmed', NULL);
insert into travel.Flight values (333, 16, 'R', 'Japan Airlines 101', '2009-02-09 17:00:00.00', 'Tokyo Narita Intl Arpt (NRT)', '2009-02-09 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'Japan Airlines', 'Confirmed', NULL);
insert into travel.Flight values (422, 17, 'D', 'United Airlines 101', '2009-04-11 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2009-04-11 17:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (422, 18, 'R', 'United Airlines 121', '2009-04-18 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2009-04-18 23:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (455, 19, 'D', 'Scand Airlines 8823', '2008-05-13 09:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-05-13 17:30:00.00', 'Arlanda-Stockholm, Sweden (ARN)', 'Scandinavian Airlines', 'Confirmed', NULL);
insert into travel.Flight values (455, 20, 'R', 'Scand Airlines 8823', '2008-05-20 17:00:00.00', 'Arlanda-Stockholm, Sweden (ARN)', '2008-05-20 23:10:00.00', 'San Francisco Intl Arpt (SFO)', 'Scandinavian Airlines', 'Confirmed', NULL);

insert into travel.Flight values (592, 21, 'D', 'AERO Airlines 001', '2008-06-16 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-06-16 17:30:00.00', 'Novosibirsk Regional Arpt (NVI)', 'AEROFLOT Airlines', 'Confirmed', NULL);
insert into travel.Flight values (592, 22, 'R', 'AERO Airlines 101', '2008-06-23 17:00:00.00', 'Novosibirsk Regional Arpt (NVI)', '2008-06-23 23:10:00.00', 'San Jose Intl Arpt (SJC)', 'AEROFLOT Airlines', 'Confirmed', NULL);
insert into travel.Flight values (201, 23, 'D', 'United Airlines23', '2008-07-01 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-07-13 01:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (201, 24, 'R', 'United Airlines 88', '2008-07-08 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2008-07-08 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (590, 25, 'D', 'Delta Airlines 201', '2008-08-11 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-08-11 17:30:00.00', 'Orlando Intl Arpt (ORL)', 'Delta Airlines', 'Confirmed', NULL);
insert into travel.Flight values (590, 26, 'R', 'Delta Airlines 202', '2008-08-18 17:00:00.00', 'Orlando Intl Arpt (ORL)', '2008-08-18 23:10:00.00', 'San Jose Intl Arpt (SJC)', 'Delta Airlines', 'Confirmed', NULL);
insert into travel.Flight values (380, 27, 'D', 'United Airlines 23', '2009-10-23 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2009-10-23 01:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (380, 28, 'R', 'United Airlines 88', '2009-10-30 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2009-10-30 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (421, 29, 'D', 'United Airlines 23', '2009-11-09 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2009-11-09 01:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (421, 30, 'R', 'United Airlines 88', '2009-11-16 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2009-11-16 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);

insert into travel.Flight values (100, 31, 'D', 'United Airlines 709', '2008-05-01 09:00:00.00', 'Aspen Arpt (ASE)', '2008-05-01 01:30:00.00', 'San Francisco Intl Arpt (SFO)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (100, 32, 'R', 'United Airlines 880', '2008-05-08 17:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-05-08 20:10:00.00', 'Aspen Arpt (ASE)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (159, 33, 'D', 'American Airlines 03', '2008-09-01 09:00:00.00', 'Aspen Regional Arpt (ASE)', '2008-09-01 01:30:00.00', 'Park City Regional Arpt (PRK)', 'American Airlines', 'Confirmed', NULL);
insert into travel.Flight values (159, 34, 'R', 'American Airlines 08', '2008-09-08 17:00:00.00', 'Park City Regional Arpt (PRK)', '2008-09-08 20:10:00.00', 'Aspen Regional Arpt (ASE)', 'American Airlines', 'Confirmed', NULL);
insert into travel.Flight values (252, 35, 'D', 'United Airlines 23', '2008-11-01 09:00:00.00', 'Aspen Regional Arpt (ASE)', '2008-11-01 01:30:00.00', 'Chicago OHare Intl Arpt (ORD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (252, 36, 'R', 'United Airlines 88', '2008-11-08 17:00:00.00', 'Chicago OHare Intl Arpt (ORD)', '2008-11-08 20:10:00.00', 'Aspen Regional Arpt (ASE)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (359, 37, 'D', 'Delta Airlines 23', '2009-01-26 09:00:00.00', 'Aspen Regional Arpt (ASE)', '2009-01-26 01:30:00.00', 'Los Angeles Intl Arpt (LAX)', 'Delta Airlines', 'Confirmed', NULL);
insert into travel.Flight values (359, 38, 'R', 'Delta Airlines 88', '2009-01-28 17:00:00.00', 'Los Angeles Intl Arpt (LAX)', '2009-01-28 20:10:00.00', 'Aspen Regional Arpt (ASE)', 'Delta Airlines', 'Confirmed', NULL);
insert into travel.Flight values (460, 39, 'D', 'United Airlines 709', '2009-05-06 09:00:00.00', 'Aspen Arpt (ASE)', '2008-05-06 01:30:00.00', 'San Francisco Intl Arpt (SFO)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (460, 40, 'R', 'United Airlines 880', '2009-05-13 17:00:00.00', 'San Francisco Intl Arpt (SFO)', '2008-05-13 20:10:00.00', 'Aspen Arpt (ASE)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (463, 41, 'D', 'United Airlines 23', '2009-06-11 09:00:00.00', 'Glenwood Springs (GSS)', '2009-06-11 01:30:00.00', 'Philadelphia Intl Arpt (PHL)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (463, 42, 'R', 'Bankrupt Air 3266', '2009-06-18 17:00:00.00', 'Philadelphia Intl Arpt (PHL)', null, 'Glenwood Springs (GSS)', 'Bankrupt Airlines', null, NULL);

insert into travel.Flight values (198, 43, 'D', 'United Airlines 23', '2008-06-11 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-06-11 01:30:00.00', 'Grenoble Arpt (GRN)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (198, 44, 'R', 'United Airlines 88', '2008-06-18 17:00:00.00', 'Grenoble Arpt (GRN)', '2008-06-18 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (208, 45, 'D', 'United Airlines 23', '2008-06-21 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2008-06-21 01:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (208, 46, 'R', 'United Airlines 23', '2008-06-28 09:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2008-06-28 01:30:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (383, 47, 'D', 'Air France 902', '2009-10-23 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2009-10-23 01:30:00.00', 'Grenoble Arpt (GRN)', 'Air France', 'Confirmed', NULL);
insert into travel.Flight values (383, 48, 'R', 'Air France 902', '2009-10-30 17:00:00.00', 'Grenoble Arpt (GRN)', '2009-10-30 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'Air France', 'Confirmed', NULL);
insert into travel.Flight values (420, 49, 'D', 'United Airlines 23', '2009-11-09 09:00:00.00', 'San Jose Intl Arpt (SJC)', '2009-11-09 01:30:00.00', 'Washington Dulles Intl Arpt (IAD)', 'United Airlines', 'Confirmed', NULL);
insert into travel.Flight values (420, 50, 'R', 'United Airlines 88', '2009-11-16 17:00:00.00', 'Washington Dulles Intl Arpt (IAD)', '2009-11-16 20:10:00.00', 'San Jose Intl Arpt (SJC)', 'United Airlines', 'Confirmed', NULL);

create table travel.CarRental (
  TripID                      INTEGER NOT NULL,
  CarRentalID                 INTEGER NOT NULL,
  Provider                    VARCHAR(35),
  City                        VARCHAR(32),
  PickupDate                  DATE,
  ReturnDate                  DATE,
  CarType                     VARCHAR(30),
  Rate                        DECIMAL(10,2),
  BookingStatus               VARCHAR(20),
  LastUpdated                 TIMESTAMP
);
 
alter table travel.CarRental
  add constraint travel_CarRentalPK
  PRIMARY KEY (CarRentalID);

alter table travel.CarRental
  add constraint travel_CarRentalFK
  FOREIGN KEY (TripID) REFERENCES travel.Trip (TripID)
  on delete cascade ;
 
 -- CarRental
 insert into travel.CarRental values (128, 1, 'Avis', 'New York', '2008-06-16', '2008-06-23', 'Economy', 89.90, 'Confirmed', NULL);
 insert into travel.CarRental values (199, 2, 'Avis', 'New York', '2008-09-14', '2008-09-17', 'Hybrid', 89.90, 'Confirmed', NULL);
 insert into travel.CarRental values (202, 3, 'Hertz', 'Toronto, ON', '2008-10-22', '2008-10-24', 'Sub Compact', 44.99 , 'Confirmed', NULL);
 insert into travel.CarRental values (203, 4, 'Hertz', 'Tokyo, JP', '2008-10-23', '2008-10-30', 'Economy', 59.90, null, NULL);
 insert into travel.CarRental values (367, 5, 'National', 'Chicago, IL', '2008-10-12', '2008-10-19', 'Economy', null, 'Confirmed', NULL);
 insert into travel.CarRental values (200, 6, 'Budget', 'Washington, DC', '2008-06-11', '2008-06-18', 'Economy', 52.50, 'Confirmed', NULL);
 insert into travel.CarRental values (310, 7, 'Budget', 'Washington, DC', '2008-08-03', '2008-08-08', 'SUV', 52.50, 'Confirmed', NULL);
 insert into travel.CarRental values (333, 8, 'Hertz', 'Tokyo, JP', '2009-02-03', '2008-02-09', null, 55.50, 'Confirmed', NULL);
 insert into travel.CarRental values (422, 9, 'Budget', 'Washington, DC', '2009-04-11', '2008-04-18', null, null, null, NULL);
 insert into travel.CarRental values (455, 10, 'Budget', 'Stockholm, SE', '2008-05-13', '2008-05-20', 'Economy', 50.50, 'Confirmed', NULL);
 insert into travel.CarRental values (201, 11, 'Budget', 'Washington, DC', '2008-07-01', '2008-07-08', 'Economy', null, null, NULL);
 insert into travel.CarRental values (590, 12, 'Budget', 'Orlando, FL', '2008-08-11', '2008-08-18', 'Economy', 50.50, 'Confirmed', NULL);
 insert into travel.CarRental values (380, 13, 'Budget', 'Washington, DC', '2009-10-23', '2009-10-30', 'Economy', 50.50, 'Confirmed', NULL);
 insert into travel.CarRental values (421, 14, 'Budget', 'Washington, DC', '2009-11-09', '2009-11-16', 'Luxury', 69.50, 'Confirmed', NULL);
 insert into travel.CarRental values (100, 15, 'Hertz', 'San Francisco, CA', '2008-05-01', '2008-05-08', null, 35.50, 'Confirmed', NULL);
 insert into travel.CarRental values (159, 16, 'SkiShuttle', 'Park City, UT', '2008-09-01', '2008-09-08', 'Wagon', 59.500, 'Confirmed', NULL);
 insert into travel.CarRental values (359, 17, 'Budget', 'Los Angeles, CA', '2009-01-26', '2009-01-28', 'Economy', 50.50, 'Confirmed', NULL);
 insert into travel.CarRental values (460, 18, 'Hertz', 'San Francisco, CA', '2009-05-06', '2008-05-13', 'Economy', 69.69, 'Confirmed', NULL);
 insert into travel.CarRental values (208, 19, 'Budget', 'Washington, DC', '2008-06-21', '2008-06-28', 'Economy', 60.50, 'Confirmed', NULL);
 insert into travel.CarRental values (383, 20, 'Eurocar', 'Grenoble, FR', '2009-10-23', '2009-10-30', 'Compact', 60.50, 'Confirmed', NULL);
 insert into travel.CarRental values (463, 21, 'RentAWreck', 'Washington, DC', '2009-06-11', '2009-06-18', '4 Wheels', 12.99, null, NULL);
 
create table travel.Hotel (
  TripID                      INTEGER NOT NULL,
  HotelID                     INTEGER NOT NULL,
  HotelName                   VARCHAR(35),
  CheckinDate                 DATE,
  CheckoutDate                DATE,
  Guests                      INTEGER,
  BookingStatus               VARCHAR(20),
  LastUpdated                 TIMESTAMP
);
 
alter table travel.Hotel
  add constraint travel_HotelPK
  PRIMARY KEY (HotelID);

alter table travel.Hotel
  add constraint travel_HotelFK
  FOREIGN KEY (TripID) REFERENCES travel.Trip (TripID)
  on delete cascade;
 
insert into travel.Hotel values (128, 1, 'New York Marriott Financial Center', '2008-06-16', '2008-06-23', 1, 'Confirmed', NULL);
insert into travel.Hotel values (199, 2, 'New York Marriott Financial Center', '2008-09-14', '2008-09-17', 1, 'Confirmed', NULL);
insert into travel.Hotel values (202, 3, 'Carl E. Quinn Hotel and Resort', '2008-10-22', '2008-10-24', 1, 'Confirmed', NULL);
insert into travel.Hotel values (203, 4, 'Mikimoto Plaza', '2008-10-23', '2008-10-30', 1, 'Confirmed', NULL);
insert into travel.Hotel values (367, 5, 'The Drake', '2008-12-12', '2008-12-19', 1, 'Confirmed', NULL);
insert into travel.Hotel values (200, 6, 'Sheraton Washington', '2008-06-11', '2008-06-18', 1, 'Confirmed', NULL);
insert into travel.Hotel values (310, 7, 'Sheraton Washington', '2008-08-03', '2008-08-03', 1, 'Confirmed', NULL);
insert into travel.Hotel values (333, 8, 'Tokyo Marriott', '2009-02-03', '2009-02-09', 1, 'Confirmed', NULL);
insert into travel.Hotel values (422, 9, 'Sheraton Washington', '2009-04-11', '2009-04-18', 1, 'Confirmed', NULL);
insert into travel.Hotel values (455, 10, 'Octavian Spa Stockholm', '2008-05-13', '2008-05-20', 1, 'Confirmed', NULL);
insert into travel.Hotel values (592, 11, 'Best Eastern Vostok S Hotel', '2008-05-16', '2008-06-23', 1, 'Confirmed', NULL);
insert into travel.Hotel values (201, 12, 'Sheraton Washington', '2008-07-01', '2008-07-08', 1, 'Confirmed', NULL);
insert into travel.Hotel values (590, 13, 'Mickeys Hideaway', '2008-08-11', '2008-08-18', 1, 'Confirmed', NULL);
insert into travel.Hotel values (380, 14, 'Sheraton Washington', '2009-10-23', '2009-10-30', 1, 'Confirmed', NULL);
insert into travel.Hotel values (421, 15, 'Mayflower Hotel', '2009-11-09', '2009-11-16', 1, 'Confirmed', NULL);
insert into travel.Hotel values (100, 16, 'Hyatt San Francisco', '2008-05-01', '2008-05-08', 1, 'Confirmed', NULL);
insert into travel.Hotel values (159, 17, 'Park City Ski Lodge', '2008-09-01', '2008-09-08', 1, 'Confirmed', NULL);
insert into travel.Hotel values (252, 18, 'Omni Chicago', '2008-11-01', '2008-11-08', 1, 'Confirmed', NULL);
insert into travel.Hotel values (359, 19, 'Sheraton Los Angeles Airport', '2009-01-26', '2009-01-28', 1, 'Confirmed', NULL);
insert into travel.Hotel values (460, 20, 'Hyatt San Francisco', '2009-05-06', '2008-05-13', 1, 'Confirmed', NULL);
insert into travel.Hotel values (208, 21, 'Sheraton Washington', '2008-06-21', '2008-06-28', 1, 'Confirmed', NULL);
insert into travel.Hotel values (383, 22, 'Grenoble Garden Chateau', '2009-10-23', '2009-10-30', 1, 'Confirmed', NULL);
insert into travel.Hotel values (463, 23, 'Mounment Hostel', '2009-6-11', '2009-06-18', 1, null, NULL);

create view travel.PersonTrip as select tripid, name from trip, person where
trip.personid = person.personid ;

CREATE TABLE travel.VALIDATION_TABLE (
  KEYCOL                      INTEGER
);
