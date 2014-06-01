
cat nohup.out  | grep 'WARN\|ERROR\|Exception\| [tx.* [0-9]\{5\}ms' | grep -v "empty lead" | tail
echo

tail -f nohup.out | grep 'WARN\|ERROR\|Exception\| \[tx.* [0-9]\{5\}ms'
