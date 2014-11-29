set terminal pdfcairo font "Times-Roman,7"
set termoption dashed
set key right bottom
set pointsize 0.5
set style line 1 lt 1 linecolor rgb "#E00010" lw 3 pt 5 #43 greyscale
set style line 2 lt 2 linecolor rgb "#00A000" lw 3 pt 5 #60 
set style line 3 lt 3 linecolor rgb "#1010F0" lw 3 pt 5 #24
set style line 4 lt 4 linecolor rgb "#827910" lw 3 pt 5 #78
set key top left
set output "../Paper/graphs/mw_speed.pdf"
set xlabel "m_w Parameter value"
set ylabel "Time in milliseconds to expand empty rule"
set yrange [0:6000]
plot "mw_speed_size" using 1:2 w linespoints title "Marketing Size weighting" ls 1, "mw_speed_bits" using 1:2 w linespoints title "Marketing Bits weighting" ls 2, \
"mw_speed_USCensus_size" using 1:2 w linespoints title "Census Size weighting" ls 3, "mw_speed_USCensus_bits" using 1:2 w linespoints title "Census Bits weighting" ls 4
set key top left
set output "../Paper/graphs/minSS_speed.pdf"
set xlabel "minSS Parameter value"
set ylabel "Time in milliseconds to expand empty rule"
set yrange [0:5000]
plot "minSS_speed_size" using 1:2 w linespoints title "Marketing Size weighting" ls 1, "minSS_speed_bits" using 1:2 w linespoints title "Marketing Bits weighting" ls 2, \
"minSS_speed_USCensus_size" using 1:2 w linespoints title "Census Bits weighting" ls 3, "minSS_speed_USCensus_bits" using 1:2 w linespoints title "Census Bits weighting" ls 4
set autoscale
set key top right
set output "../Paper/graphs/minSS_error_count.pdf"
set xlabel "minSS Parameter value"
set ylabel "Error in rule counts"
plot "minSS_error_size" using 1:2 w linespoints title "Marketing Size weighting" ls 1, "minSS_error_bits" using 1:2 w linespoints title "Marketing Bits weighting" ls 2, \
"minSS_error_USCensus_size" using 1:2 w linespoints title "Census Size weighting" ls 3, "minSS_error_USCensus_bits" using 1:2 w linespoints title "Census Bits weighting" ls 4
set output "../Paper/graphs/minSS_error_percent.pdf"
set xlabel "minSS Parameter value"
set ylabel "Error(percent) in rule counts"
plot "minSS_error_size" using 1:3 w linespoints title "Marketing Size weighting" ls 1, "minSS_error_bits" using 1:3 w linespoints title "Marketing Bits weighting" ls 2, \
"minSS_error_USCensus_size" using 1:3 w linespoints title "Census Size weighting" ls 3, "minSS_error_USCensus_bits" using 1:3 w linespoints title "Census Bits weighting" ls 4
set output "../Paper/graphs/minSS_error_rule.pdf"
set xlabel "minSS Parameter value"
set ylabel "Number of incorrect rules"
plot "minSS_error_size" using 1:4 w linespoints title "Marketing Size weighting" ls 1, "minSS_error_bits" using 1:4 w linespoints title "Marketing Bits weighting" ls 2, \
"minSS_error_USCensus_size" using 1:4 w linespoints title "Census Size weighting" ls 3, "minSS_error_USCensus_bits" using 1:4 w linespoints title "Census Bits weighting"