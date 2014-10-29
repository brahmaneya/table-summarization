set terminal pdfcairo font "Times-Roman,7"
set termoption dashed
set key right bottom
set pointsize 0.5
set style line 1 lt 1 linecolor rgb "#E00010" lw 3 pt 5 #43 greyscale
set style line 2 lt 2 linecolor rgb "#00A000" lw 3 pt 5 #60 
set style line 3 lt 3 linecolor rgb "#1010F0" lw 3 pt 5 #24
set style line 4 lt 4 linecolor rgb "#827910" lw 3 pt 5 #78
set output "../Paper/graphs/mw_speed.pdf"
set xlabel "m_w Parameter value"
set ylabel "Time in milliseconds to expand empty rule"
plot "mw_speed_size" using 1:2 w linespoints title "Size scoring" ls 1, "mw_speed_bits" using 1:2 w linespoints title "Bits scoring" ls 2