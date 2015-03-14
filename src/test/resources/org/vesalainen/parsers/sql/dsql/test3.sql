select 
    rs.Event, re.Boat, re.Class, re.Fee, re.Paid
from
    RaceSeries rs, RaceEntry re
where
    rs.Event in ('ProSailor Race', 'WB Sails round the buoys race', 'Melges 24 Nordic Championship' ) and
    rs is ancestor of re and
    re.Fee <> re.Paid
;