
first=sport
caching=false
refresh=false
maxDepth=9
periodSeconds=300

c0refresh() {
  refresh=true
  maxDepth=0
}

c0refreshall() {
  refresh=true
  maxDepth=9
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
fi

cd 
cd angulardemo

sleep 8
while ls -l .rsyncing
do
  sleep 1
done

if pgrep -f angulardemo.jar
then
  kill `pgrep -f angulardemo.jar`
  sleep 1
fi

mv nohup.out tmp/.

nohup /usr/java/default/jre/bin/java \
  -Dfeeds.first=$first \
  -Dfeeds.maxDepth=$maxDepth \
  -Dstorage.caching=$caching \
  -Dstorage.refresh=$refresh \
  -Dstorage.contentUrl=http://chronica.co \
  -Dstorage.storageDir=/home/evanx/angulardemo/storage \
  -Dstorage.appDir=/home/evanx/angulardemo/app \
  -Djsse.enableSNIExtension=false -Xms256M -Xmx512M \
  -ea -jar angulardemo.jar 2>&1 &

sleep 2 

tail -f nohup.out | grep 'Error\|WARN\|ERROR\|Exception\| \[tx' | 
  grep -v 'begin article\.' |
  grep -v 'sub article\.'

