
if pgrep -f angulardemo.jar
then
  kill `pgrep -f angulardemo.jar`
  sleep 1
fi

ps aux | grep -v grep | grep angulardemo

