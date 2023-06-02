SERV=tiger.melt
DEST=${SERV}:reports/reports.jar

#build:
#	docker build -t hkim0331/reports .

clean:
	${RM} -r target

uberjar: clean
	# no use
	# shadow-cljs release app
	lein uberjar

deploy: uberjar
	scp target/uberjar/reports.jar ${DEST} && \
	ssh ${SERV} sudo systemctl restart reports && \
	ssh ${SERV} systemctl status reports
