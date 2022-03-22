/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.insa.cipherdit.reddit.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.insa.cipherdit.account.RedditAccount;
import org.insa.cipherdit.cache.CacheManager;
import org.insa.cipherdit.cache.CacheRequest;
import org.insa.cipherdit.cache.CacheRequestJSONParser;
import org.insa.cipherdit.cache.downloadstrategy.DownloadStrategyAlways;
import org.insa.cipherdit.common.Constants;
import org.insa.cipherdit.common.General;
import org.insa.cipherdit.common.Optional;
import org.insa.cipherdit.common.Priority;
import org.insa.cipherdit.common.TimestampBound;
import org.insa.cipherdit.common.UnexpectedInternalStateException;
import org.insa.cipherdit.http.FailedRequestBody;
import org.insa.cipherdit.io.CacheDataSource;
import org.insa.cipherdit.io.RequestResponseHandler;
import org.insa.cipherdit.io.WritableHashSet;
import org.insa.cipherdit.jsonwrap.JsonArray;
import org.insa.cipherdit.jsonwrap.JsonObject;
import org.insa.cipherdit.jsonwrap.JsonValue;
import org.insa.cipherdit.reddit.RedditSubredditManager;
import org.insa.cipherdit.reddit.things.InvalidSubredditNameException;
import org.insa.cipherdit.reddit.things.RedditSubreddit;
import org.insa.cipherdit.reddit.things.RedditThing;
import org.insa.cipherdit.reddit.things.SubredditCanonicalId;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class RedditAPIIndividualSubredditListRequester implements CacheDataSource<
		RedditSubredditManager.SubredditListType, WritableHashSet, SubredditRequestFailure> {

	private final Context context;
	private final RedditAccount user;

	public RedditAPIIndividualSubredditListRequester(
			final Context context,
			final RedditAccount user) {
		this.context = context;
		this.user = user;
	}

	@Override
	public void performRequest(
			final RedditSubredditManager.SubredditListType type,
			final TimestampBound timestampBound,
			final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler) {

		if(type == RedditSubredditManager.SubredditListType.DEFAULTS) {

			final long now = System.currentTimeMillis();

			final HashSet<String> data =
					new HashSet<>(Constants.Reddit.DEFAULT_SUBREDDITS.size() + 1);

			for(final SubredditCanonicalId id : Constants.Reddit.DEFAULT_SUBREDDITS) {
				data.add(id.toString());
			}

			data.add("/r/redreader");

			final WritableHashSet result = new WritableHashSet(data, now, "DEFAULTS");
			handler.onRequestSuccess(result, now);

			return;
		}

		if(type == RedditSubredditManager.SubredditListType.MOST_POPULAR) {
			doSubredditListRequest(
					RedditSubredditManager.SubredditListType.MOST_POPULAR,
					handler,
					null);

		} else if(user.isAnonymous()) {
			switch(type) {

				case SUBSCRIBED:
					performRequest(
							RedditSubredditManager.SubredditListType.DEFAULTS,
							timestampBound,
							handler);
					return;

				case MODERATED: {
					final long curTime = System.currentTimeMillis();
					handler.onRequestSuccess(
							new WritableHashSet(
									new HashSet<>(),
									curTime,
									RedditSubredditManager.SubredditListType.MODERATED.name()),
							curTime);
					return;
				}

				case MULTIREDDITS: {
					final long curTime = System.currentTimeMillis();
					handler.onRequestSuccess(
							new WritableHashSet(
									new HashSet<>(),
									curTime,
									RedditSubredditManager.SubredditListType.MULTIREDDITS.name()),
							curTime);
					return;
				}

				default:
					throw new RuntimeException(
							"Internal error: unknown subreddit list type '"
									+ type.name()
									+ "'");
			}

		} else {
			doSubredditListRequest(type, handler, null);
		}
	}

	private void doSubredditListRequest(
			final RedditSubredditManager.SubredditListType type,
			final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler,
			final String after) {

		final URI uri;

		{
			final URI baseUri;

			switch(type) {
				case SUBSCRIBED:
					baseUri = Constants.Reddit.getUri(
							Constants.Reddit.PATH_SUBREDDITS_MINE_SUBSCRIBER);
					break;
				case MODERATED:
					baseUri = Constants.Reddit.getUri(
							Constants.Reddit.PATH_SUBREDDITS_MINE_MODERATOR);
					break;
				case MOST_POPULAR:
					baseUri = Constants.Reddit.getUri(
							Constants.Reddit.PATH_SUBREDDITS_POPULAR);
					break;
				default:
					throw new UnexpectedInternalStateException(type.name());
			}

			if(after == null) {
				uri = baseUri;

			} else {
				final Uri.Builder builder = Uri.parse(baseUri.toString()).buildUpon();
				builder.appendQueryParameter("after", after);
				uri = General.uriFromString(builder.toString());
			}
		}

		final CacheRequest aboutSubredditCacheRequest = new CacheRequest(
				uri,
				user,
				null,
				new Priority(Constants.Priority.API_SUBREDDIT_INVIDIVUAL),
				DownloadStrategyAlways.INSTANCE,
				Constants.FileType.SUBREDDIT_LIST,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
				context,
				new CacheRequestJSONParser(context, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session, final boolean fromCache) {

						try {

							final HashSet<String> output = new HashSet<>();
							final ArrayList<RedditSubreddit> toWrite = new ArrayList<>();

							final JsonObject redditListing =
									result.asObject().getObject("data");

							final JsonArray subreddits =
									redditListing.getArray("children");

							if(type == RedditSubredditManager.SubredditListType.SUBSCRIBED
									&& subreddits.size() == 0
									&& after == null) {
								performRequest(
										RedditSubredditManager.SubredditListType.DEFAULTS,
										TimestampBound.ANY,
										handler);
								return;
							}

							for(final JsonValue v : subreddits) {
								final RedditThing thing = v.asObject(RedditThing.class);
								final RedditSubreddit subreddit = thing.asSubreddit();

								subreddit.downloadTime = timestamp;

								try {
									output.add(subreddit.getCanonicalId().toString());
									toWrite.add(subreddit);
								} catch(final InvalidSubredditNameException e) {
									Log.e(
											"SubredditListRequester",
											"Ignoring invalid subreddit",
											e);
								}

							}

							RedditSubredditManager.getInstance(context, user)
									.offerRawSubredditData(toWrite, timestamp);
							final String receivedAfter = redditListing.getString("after");
							if(receivedAfter != null && type !=
									RedditSubredditManager.SubredditListType.MOST_POPULAR) {

								doSubredditListRequest(
										type,
										new RequestResponseHandler<
												WritableHashSet,
												SubredditRequestFailure>() {
											@Override
											public void onRequestFailed(
													final SubredditRequestFailure failureReason) {
												handler.onRequestFailed(failureReason);
											}

											@Override
											public void onRequestSuccess(
													final WritableHashSet result,
													final long timeCached) {
												output.addAll(result.toHashset());
												handler.onRequestSuccess(new WritableHashSet(
														output,
														timeCached,
														type.name()), timeCached);

												if(after == null) {
													Log.i("SubredditListRequester", "Got "
															+ output.size()
															+ " subreddits in multiple requests");
												}
											}
										},
										receivedAfter);

							} else {
								handler.onRequestSuccess(new WritableHashSet(
										output,
										timestamp,
										type.name()), timestamp);

								if(after == null) {
									Log.i("SubredditListRequester", "Got "
											+ output.size() + " subreddits in 1 request");
								}
							}

						} catch(final Exception e) {
							handler.onRequestFailed(new SubredditRequestFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									e,
									null,
									"Parse error",
									uri != null ? uri.toString() : null,
									Optional.of(new FailedRequestBody(result))));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						handler.onRequestFailed(new SubredditRequestFailure(
								type,
								t,
								httpStatus,
								readableMessage,
								uri != null ? uri.toString() : null,
								body));
					}
				}));

		CacheManager.getInstance(context).makeRequest(aboutSubredditCacheRequest);
	}

	@Override
	public void performRequest(
			final Collection<RedditSubredditManager.SubredditListType> keys,
			final TimestampBound timestampBound,
			final RequestResponseHandler<
					HashMap<RedditSubredditManager.SubredditListType, WritableHashSet>,
					SubredditRequestFailure> handler) {
		// TODO batch API? or just make lots of requests and build up a hash map?
		throw new UnsupportedOperationException();
	}

	@Override
	public void performWrite(final WritableHashSet value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void performWrite(final Collection<WritableHashSet> values) {
		throw new UnsupportedOperationException();
	}
}
