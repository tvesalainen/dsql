dsql
====

Datastore SQL. SQL engine for Google app engine datastore

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



