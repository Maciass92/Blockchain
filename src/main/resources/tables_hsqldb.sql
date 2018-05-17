CREATE TABLE public.network_hashrate
(
    id bigint NOT NULL DEFAULT nextval('network_hashrate_id_seq'::regclass),
    hashrate double precision NOT NULL,
    rep_date timestamp with time zone NOT NULL,
    CONSTRAINT network_hashrate_pkey PRIMARY KEY (id)
);