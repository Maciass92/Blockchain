insert into network_hashrate (
    rep_date, hashrate
)
select
    current_timestamp - (s.a || ' hour')::interval,
    s.a::double precision
from generate_series(0, 15, 1) AS s(a);



insert into pool_def (
    name, date_from
)
select
    left(md5(random()::text), 10),
    current_timestamp - (s.a || ' hour')::interval,
from generate_series(0, 15, 1) AS s(a);



insert into pool_hashrate (
    hashrate
)
select
    s.a::double precision
from generate_series(0, 15, 1) AS s(a);


