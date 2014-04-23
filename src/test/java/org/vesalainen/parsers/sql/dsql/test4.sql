select 
    rs.Event, re.Boat, re.Class, re.Fee, re.Paid
from
    RaceSeries rs, RaceFleet rf, RaceEntry re
where
    rs.Event = 'HSK - Blue Peter Race 2012' and
    rs is parent of rf and
    rf is parent of re and
    re.Boat is null
;