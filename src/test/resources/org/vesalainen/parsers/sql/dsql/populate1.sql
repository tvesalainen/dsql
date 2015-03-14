INSERT INTO Member (key, Firstname, Lastname) VALUES(key(Root(1)/Member(1)), 'Timo', 'Vesalainen');
INSERT INTO Member (key, Firstname, Lastname) VALUES(key(Root(1)/Member(2)), 'Johanna', 'Vesalainen');
INSERT INTO Member (key, Firstname, Lastname) VALUES(key(Root(1)/Member(3)), 'Charlie', 'Brown');

INSERT INTO Boattype (key, TypeID) VALUES(key(Root(1)/Boattype(1)), 'PV');
INSERT INTO Boattype (key, TypeID) VALUES(key(Root(1)/Boattype(2)), 'MV');

INSERT INTO Boat (key, Name, Type, BoatID, Owner) VALUES(key(Root(1)/Boat(1)), 'Valpuri', key(Root(1)/Boattype(1)), 123, key(Root(1)/Member(1)));
INSERT INTO Boat (key, Name, Type, BoatID, Owner) VALUES(key(Root(1)/Boat(2)), 'Iiris', key(Root(1)/Boattype(1)), 333, key(Root(1)/Member(1)));
INSERT INTO Boat (key, Name, Type, BoatID, Owner) VALUES(key(Root(1)/Boat(3)), 'Tokyo', key(Root(1)/Boattype(1)), 444, key(Root(1)/Member(3)));

INSERT INTO RaceSeries (key, Event) VALUES(key(Root(1)/RaceSeries(1)), 'ProSailor Race');
INSERT INTO RaceFleet (key, Fleet) VALUES(key(Root(1)/RaceSeries(1)/RaceFleet(1)), 'IRC');
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(1)/RaceFleet(1)/RaceEntry(1)), 'Valpuri', 60, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(1)/RaceFleet(1)/RaceEntry(2)), 'Lightning', 90, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(1)/RaceFleet(1)/RaceEntry(3)), 'No Wind', 60, 60);

INSERT INTO RaceSeries (key, Event) VALUES(key(Root(1)/RaceSeries(2)), 'WB Sails round the buoys race');
INSERT INTO RaceFleet (key, Fleet) VALUES(key(Root(1)/RaceSeries(2)/RaceFleet(1)), 'IRC');
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(2)/RaceFleet(1)/RaceEntry(1)), 'Valpuri', 60, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(2)/RaceFleet(1)/RaceEntry(2)), 'Lightning', 90, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(2)/RaceFleet(1)/RaceEntry(3)), 'No Wind', 60, 60);

INSERT INTO RaceSeries (key, Event) VALUES(key(Root(1)/RaceSeries(3)), 'Melges 24 Nordic Championship');
INSERT INTO RaceFleet (key, Fleet) VALUES(key(Root(1)/RaceSeries(3)/RaceFleet(1)), 'Melges 24');
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(3)/RaceFleet(1)/RaceEntry(1)), 'Valpuri', 60, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(3)/RaceFleet(1)/RaceEntry(2)), 'Lightning', 90, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(3)/RaceFleet(1)/RaceEntry(3)), 'No Wind', 60, 60);

INSERT INTO RaceSeries (key, Event) VALUES(key(Root(1)/RaceSeries(4)), 'HSK - Blue Peter Race 2012');
INSERT INTO RaceFleet (key, Fleet) VALUES(key(Root(1)/RaceSeries(4)/RaceFleet(1)), 'LYS');
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(4)/RaceFleet(1)/RaceEntry(1)), 'Valpuri', 60, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(4)/RaceFleet(1)/RaceEntry(2)), 'Lightning', 90, 60);
INSERT INTO RaceEntry (key, Boat, Fee, Paid) VALUES(key(Root(1)/RaceSeries(4)/RaceFleet(1)/RaceEntry(3)), 'No Wind', 60, 60);


