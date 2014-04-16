/*
    comment
*/
select -- comment
Jasenet.Etunimi, Jasenet.Sukunimi, Veneet.Nimi, Venetyyppit.Tyyppi, Veneet.VeneID 
from # comment
Veneet, Jasenet, Venetyyppit 
where /* comment */
Veneet.Omistaja is key of Jasenet and 
Veneet.Tyyppi is key of Venetyyppit and 
Veneet.Nimi = 'Valpuri' and 
Venetyyppit.TyyppiID = 'PV' 
order by Jasenet.Sukunimi, Jasenet.Etunimi, Veneet.Nimi
;