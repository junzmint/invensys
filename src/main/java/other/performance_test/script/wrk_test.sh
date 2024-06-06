wrk -t6 -c120 -d30s --latency --timeout 30s -s wrk_test_script.lua http://localhost:8080
