

cat nohup.out | grep 'Error\|WARN\|ERROR\|Exception\| \[tx\|^+' | 
  grep -v ' begin \| sub \| finish article\.'

echo
cat nohup.out | grep 'Error\|ERROR\|Exception\|[0-9]\{5\}ms'

echo
cat nohup.out | grep 'Error\|ERROR\|Exception\|[0-9]\{6\}ms'

