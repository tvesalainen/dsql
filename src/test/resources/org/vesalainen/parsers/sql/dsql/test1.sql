/*
    comment
*/
select -- comment
Member.Firstname, Member.Lastname, Boat.Name, Boattype.TypeID, Boat.BoatID 
from # comment
Boat, Member, Boattype 
where /* comment */
Boat.Owner is key of Member and 
Boat.Type is key of Boattype and 
Boat.Name = 'Valpuri' and 
Boattype.TypeID = 'PV' 
order by Member.Lastname, Member.Firstname, Boat.Name
;