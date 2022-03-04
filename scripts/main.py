import reddit

client = reddit.Client('agent', 'key')

subreddit = client.Subreddit('top', 'subreddit-here') # supports 'top', 'new' or 'hot' type of submissions

print(subreddit.selftext(0))