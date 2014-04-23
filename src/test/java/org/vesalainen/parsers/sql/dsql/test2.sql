select 
    rs.Event, re.Boat, re.Class, re.Fee, re.Paid
from
    RaceSeries rs, RaceEntry re
where
    rs.Event = 'ProSailor Race' and
    rs is ancestor of re and
    re.Fee <> re.Paid
;