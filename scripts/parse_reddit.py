#!/usr/bin/env python3

import json
import requests
import sys

if len(sys.argv) != 3:
    print("Deux arguments attendus : $0 [subreddit] [nb_posts]")
    print("Exemple : " + sys.argv[0] + " all 10")
    exit(1)

subreddit = sys.argv[1]
nb_posts = sys.argv[2]
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"}

url = "https://www.reddit.com/r/" + subreddit + "/top/.json?t=all&limit=" + nb_posts
r = requests.get(url, headers=headers)

#print(r)
#print(r.content)

raw = json.loads(r.content.decode("utf-8"))

# Données à extraire :
#   title
#   content
#   link_flair_richtext
#   author
#   subreddit

count = 0
tab = "    "
print("{")
for post in raw["data"]["children"]:
    count += 1
    info = post["data"]
    #print(json.dumps(info, indent=4, sort_keys=True))

    print(tab + "\"post" + str(count) + "\": [")
    print(tab*2 + "{")

    title = info["title"]
    text = info["selftext"]
    flairs = info["link_flair_richtext"]
    author = info["author"]
    subreddit = info["subreddit"]
    content = info["url"]

    print(tab*3 + "\"title\": \"" + title + "\",")
    print(tab*3 + "\"author\": \"" + author + "\",")
    print(tab*3 + "\"subreddit\": \"" + subreddit + "\",")
    print(tab*3 + "\"text\": \"" + text + "\",")
    print(tab*3 + "\"content\": \"" + content + "\",")
    print(tab*3 + "\"flairs\": \"" + str(flairs) + "\"")

    print(tab*2 + "}")
    print("")

print("}")
#client = reddit.Client('agent', 'key')
#subreddit = client.Subreddit('top', 'subreddit-here') # supports 'top', 'new' or 'hot' type of submissions
#print(subreddit.selftext(0))
