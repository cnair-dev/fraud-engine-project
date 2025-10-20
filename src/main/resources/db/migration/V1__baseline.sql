--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13
-- Dumped by pg_dump version 15.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: customers; Type: TABLE; Schema: public; Owner: fraud
--

CREATE TABLE public.customers (
    id uuid NOT NULL,
    account_type character varying(50) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    historical_chargebacks integer,
    risk_segment character varying(50)
);


ALTER TABLE public.customers OWNER TO fraud;

--
-- Name: flagged_transactions; Type: TABLE; Schema: public; Owner: fraud
--

CREATE TABLE public.flagged_transactions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    customer_id uuid NOT NULL,
    decision character varying(20) NOT NULL,
    details jsonb,
    score integer NOT NULL,
    txn_id uuid NOT NULL
);


ALTER TABLE public.flagged_transactions OWNER TO fraud;

--
-- Name: flagged_txn_reasons; Type: TABLE; Schema: public; Owner: fraud
--

CREATE TABLE public.flagged_txn_reasons (
    flagged_txn_id uuid NOT NULL,
    reason_code character varying(100)
);


ALTER TABLE public.flagged_txn_reasons OWNER TO fraud;

--
-- Name: rule_sets; Type: TABLE; Schema: public; Owner: fraud
--

CREATE TABLE public.rule_sets (
    id uuid NOT NULL,
    account_type character varying(50) NOT NULL,
    config jsonb,
    effective_from timestamp(6) with time zone NOT NULL,
    version character varying(20) NOT NULL
);


ALTER TABLE public.rule_sets OWNER TO fraud;

--
-- Name: transactions; Type: TABLE; Schema: public; Owner: fraud
--

CREATE TABLE public.transactions (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    category character varying(100),
    channel character varying(50),
    created_at timestamp(6) with time zone NOT NULL,
    currency character varying(3) NOT NULL,
    description character varying(500),
    device_id uuid,
    ip_country character varying(255),
    location_lat double precision,
    location_lon double precision,
    mcc character varying(10),
    merchant_id uuid,
    merchant_name character varying(255),
    payment_method character varying(50),
    "timestamp" timestamp(6) with time zone NOT NULL,
    customer_id uuid NOT NULL
);


ALTER TABLE public.transactions OWNER TO fraud;

--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- Name: flagged_transactions flagged_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.flagged_transactions
    ADD CONSTRAINT flagged_transactions_pkey PRIMARY KEY (id);


--
-- Name: rule_sets rule_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.rule_sets
    ADD CONSTRAINT rule_sets_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: rule_sets ukkuc04nu7om0i6ir8030d6cbqb; Type: CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.rule_sets
    ADD CONSTRAINT ukkuc04nu7om0i6ir8030d6cbqb UNIQUE (account_type, version);


--
-- Name: idx_customer_account_type; Type: INDEX; Schema: public; Owner: fraud
--

CREATE INDEX idx_customer_account_type ON public.customers USING btree (account_type);


--
-- Name: idx_customer_risk_segment; Type: INDEX; Schema: public; Owner: fraud
--

CREATE INDEX idx_customer_risk_segment ON public.customers USING btree (risk_segment);


--
-- Name: idx_flag_customer_time; Type: INDEX; Schema: public; Owner: fraud
--

CREATE INDEX idx_flag_customer_time ON public.flagged_transactions USING btree (customer_id, created_at);


--
-- Name: idx_txn_customer_time; Type: INDEX; Schema: public; Owner: fraud
--

CREATE INDEX idx_txn_customer_time ON public.transactions USING btree (customer_id, "timestamp");


--
-- Name: flagged_txn_reasons fko9eklhnkr67y4157jula3j78f; Type: FK CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.flagged_txn_reasons
    ADD CONSTRAINT fko9eklhnkr67y4157jula3j78f FOREIGN KEY (flagged_txn_id) REFERENCES public.flagged_transactions(id);


--
-- Name: transactions fkpnnreq9lpejqyjfct60v7n7x1; Type: FK CONSTRAINT; Schema: public; Owner: fraud
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT fkpnnreq9lpejqyjfct60v7n7x1 FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- PostgreSQL database dump complete
--

