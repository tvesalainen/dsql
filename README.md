dsql
====

Datastore SQL engine (DSQL) for Google appengine datastore. DSQL
extends gae datastore in implementing local joins between datastore kinds
as well as providing local filtering for unindexed properties. DSQL uses
familiar sql-syntax. SQL joins are optimized using datastore statistics.

It is assumed that datastore schema is designed like relational db. However
parent/child relationships are supported. Also supported are special datatypes
in package com.google.appengine.api.datastore like Email, Phonenumber, ...

User interface class for testing: org.vesalainen.parsers.sql.dsql.ui.WorkBench

Example of join:

select
  Reference.__id__,
  RaceEntry.Club,
  RaceEntry.Paid,
  RaceEntry.Class,
  RaceEntry.Fleet,
  RaceEntry.HelmPhone,
  RaceEntry.HelmEmail,
  RaceEntry.Boat,
  RaceEntry.Nat,
  RaceEntry.SailNo,
  RaceEntry.HelmAgeGroup,
  RaceEntry.HelmAddress,
  RaceEntry.HelmName,
  RaceEntry.Timestamp,
  RaceEntry.Rating,
  RaceEntry.Notes,
  RaceEntry.CrewName,
  RaceEntry.Fee
from
  RaceEntry re,
  Reference r
where
  re.SailNo = :"Sail number" long and
  r.Refer = re.__key__
;



