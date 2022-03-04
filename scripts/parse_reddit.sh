#!/bin/bash

if [ "$#" -ne 2 ]; then
	echo "Deux arguments attendus : $0 [subreddit] [nb_posts]"
	echo "Exemple : $0 all 10"
	exit 1
fi

SUBREDDIT=$1
NB_POSTS=$2

curl -s -H "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36" https://www.reddit.com/r/$SUBREDDIT/top/.json?limit=$NB_POSTS \
	| python -m json.tool
