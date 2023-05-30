#!/bin/sh
pg_dump -h localhost -U postgres reports > `date +reports-%F.sql`
