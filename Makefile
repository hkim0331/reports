SERV=tiger.melt
DEST=${SERV}:reports/reports.jar

#build:
#	docker build -t hkim0331/reports .

node_modules:
	npm install
	npm audit fix

clean:
	${RM} -r target

uberjar: clean
	lein uberjar

deploy: uberjar
	# npx shadow-cljs release app
	scp target/uberjar/reports.jar ${DEST} && \
	ssh ${SERV} sudo systemctl restart reports && \
	ssh ${SERV} systemctl status reports
