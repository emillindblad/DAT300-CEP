package metrics

import (
	"log"
	"strconv"
)

func ParseCsvStrToInt(input string) int64 {
	parsedInt, err := strconv.ParseInt(input, 10, 64)
	if err != nil {
		log.Fatal(err)
	}
	return parsedInt
}
