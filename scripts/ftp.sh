
server=za.chronica.co
username=zachriyv
password=steenberg

s0za() {
  server=za.chronica.co
  username=zachriyv
  password=steenberg
  dir=public_html
}

s0de() {
  server=f10-preview.awardspace.net
  username=1681173
  password='St33nberg.'
  dir=de.chronica.co
}

c2syncSection() {
  s0$1
  ncftpput -R -v -u "$username" -p "$password" $server "$dir/." /pri/angulardemo/storage/$2/articles.json*
}

c1syncSections() {
  s0$1
  ncftpput -R -v -u "$username" -p "$password" $server "$dir/." /pri/angulardemo/storage/*/articles.json*
}

c1syncStorage() {
  s0$1
  ncftpput -R -v -u "$username" -p "$password" $server "$dir/." /pri/angulardemo/storage/*/articles.json*
  ncftpput -R -v -u "$username" -p "$password" $server "$dir/." /pri/angulardemo/storage/article/*
}

c1syncApp() {
  s0$1
  ncftpput -v -u "$username" -p "$password" $server "$dir/." /pri/nb/git/angulardemo/src/reader/web/*
}

c1deployApp() {
  s0$1
  ncftpput -R -v -u "$username" -p "$password" $server "$dir/." /pri/nb/git/angulardemo/src/reader/web/*
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else 
  c0deployApp
fi


