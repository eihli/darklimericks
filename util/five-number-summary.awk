# find ~/src/prhyme/dark-corpus -type f -print0 | xargs -0 wc -l | grep -v '[0-9]\{4,\}.*total$' | sort -n

BEGIN {
}
{
    a += $1;
    b[++i] = $1;
}
END {
    m = a/NR;
    q1 = sprintf("%d", NR * 0.25);
    q2 = sprintf("%d", NR * 0.5);
    q3 = sprintf("%d", NR * 0.75);
    print sprintf("Min: %d, Q1: %d, Median: %d, Q3: %d, Max: %d", b[0], b[q1], b[q2], b[q3], b[NR]);
}
