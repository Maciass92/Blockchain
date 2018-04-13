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
    current_timestamp - (s.a || ' hour')::interval
from generate_series(0, 15, 1) AS s(a);



insert into pool_hashrate (
    hashrate, network_id, pool_id
)
select
    s.a*s.a::double precision, 17 - s.a, s.a
from generate_series(1, 16, 1) AS s(a);

insert into pool_hashrate (hashrate, network_id, pool_id)
values (10.0, 2, 13);

insert into pool_hashrate (hashrate, network_id, pool_id)
values (150.0, 2, 12);

insert into pool_hashrate (hashrate, network_id, pool_id)
values (10000.0, 2, 11);