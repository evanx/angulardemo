

c0wait() {
  while ! ls -l /pri/nb/angulardemo/dist/angulardemo.jar 
  do
    sleep 1
  done
}

c1rsync() {
  server=$1
  ssh $server touch /pri/angulardemo/.rsyncing
  rsync -ra /pri/nb/git/angulardemo/src/reader/web/* $server:/pri/angulardemo/app/.
  ssh $server 'kill -HUP `pgrep -f angulardemo.jar`'
  rsync /pri/nb/angulardemo/dist/angulardemo.jar $server:angulardemo/.
  rsync /pri/nb/vellumcore/dist/vellumcore.jar $server:angulardemo/lib/.
  ssh $server rm -f /pri/angulardemo/.rsyncing
}

c0default() {
  c0wait
  c1rsync ngena.com
}


if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command
else
  c0default
fi


