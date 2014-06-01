
server=chronica.co

c0chronica() {
  server=chronica.co
}

c0do() {
  server=ngena.com
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command
else
  exit 0
fi

ssh $server touch /pri/angulardemo/.rsyncing

rsync -ra /pri/nb/git/angulardemo/src/reader/web/* $server:/pri/angulardemo/app/.

ssh $server 'kill -HUP `pgrep -f angulardemo.jar`'

while ! ls -l /pri/nb/angulardemo/dist/angulardemo.jar 
do
  sleep 1
done

rsync /pri/nb/angulardemo/dist/angulardemo.jar $server:angulardemo/.

rsync /pri/nb/vellumcore/dist/vellumcore.jar $server:angulardemo/lib/.

ssh $server rm -f /pri/angulardemo/.rsyncing

