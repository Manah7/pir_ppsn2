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

package org.insa.cipherdit.image;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.insa.cipherdit.account.RedditAccountManager;
import org.insa.cipherdit.cache.CacheManager;
import org.insa.cipherdit.cache.CacheRequest;
import org.insa.cipherdit.cache.CacheRequestJSONParser;
import org.insa.cipherdit.cache.downloadstrategy.DownloadStrategyIfNotCached;
import org.insa.cipherdit.common.Constants;
import org.insa.cipherdit.common.General;
import org.insa.cipherdit.common.Optional;
import org.insa.cipherdit.common.Priority;
import org.insa.cipherdit.http.FailedRequestBody;
import org.insa.cipherdit.jsonwrap.JsonObject;
import org.insa.cipherdit.jsonwrap.JsonValue;

import java.util.UUID;

public final class ImgurAPIV3 {

	public static void getAlbumInfo(
			final Context context,
			final String albumUrl,
			final String albumId,
			@NonNull final Priority priority,
			final boolean withAuth,
			final GetAlbumInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/3/album/" + albumId;

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				withAuth
						? CacheRequest.DOWNLOAD_QUEUE_IMGUR_API
						: CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				context,
				new CacheRequestJSONParser(context, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						try {
							final JsonObject outer = result.asObject().getObject("data");
							listener.onSuccess(AlbumInfo.parseImgurV3(albumUrl, outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Imgur data parse failed",
									Optional.of(new FailedRequestBody(result)));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						listener.onFailure(
								type,
								t,
								httpStatus,
								readableMessage,
								body);
					}
				})));
	}

	public static void getImageInfo(
			final Context context,
			final String imageId,
			@NonNull final Priority priority,
			final boolean withAuth,
			final GetImageInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/3/image/" + imageId;

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				withAuth
						? CacheRequest.DOWNLOAD_QUEUE_IMGUR_API
						: CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				context,
				new CacheRequestJSONParser(context, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						try {
							final JsonObject outer = result.asObject().getObject("data");
							listener.onSuccess(ImageInfo.parseImgurV3(outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Imgur data parse failed",
									Optional.of(new FailedRequestBody(result)));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						listener.onFailure(
								type,
								t,
								httpStatus,
								readableMessage,
								body);
					}
				})));
	}
}