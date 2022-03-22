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

package org.insa.cipherdit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import org.insa.cipherdit.R;
import org.insa.cipherdit.account.RedditAccountChangeListener;
import org.insa.cipherdit.account.RedditAccountManager;
import org.insa.cipherdit.common.DialogUtils;
import org.insa.cipherdit.common.General;
import org.insa.cipherdit.common.LinkHandler;
import org.insa.cipherdit.common.PrefsUtility;
import org.insa.cipherdit.fragments.CommentListingFragment;
import org.insa.cipherdit.reddit.PostCommentSort;
import org.insa.cipherdit.reddit.UserCommentSort;
import org.insa.cipherdit.reddit.prepared.RedditPreparedPost;
import org.insa.cipherdit.reddit.url.PostCommentListingURL;
import org.insa.cipherdit.reddit.url.RedditURLParser;
import org.insa.cipherdit.views.RedditPostView;

import java.util.ArrayList;

public class MoreCommentsListingActivity extends RefreshableActivity
		implements RedditAccountChangeListener,
		OptionsMenuUtility.OptionsMenuCommentsListener,
		RedditPostView.PostSelectionListener {

	private static final String EXTRA_SEARCH_STRING = "mcla_search_string";

	private final ArrayList<RedditURLParser.RedditURL> mUrls = new ArrayList<>(32);

	private CommentListingFragment mFragment;

	private String mSearchString = null;

	@Override
	protected boolean baseActivityAllowToolbarHideOnScroll() {
		return true;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		setTitle(R.string.app_name);

		// TODO load from savedInstanceState

		final View layout = getLayoutInflater().inflate(R.layout.main_single, null);
		setBaseActivityListing(layout);

		RedditAccountManager.getInstance(this).addUpdateListener(this);

		if(getIntent() != null) {

			final Intent intent = getIntent();
			mSearchString = intent.getStringExtra(EXTRA_SEARCH_STRING);

			final ArrayList<String> commentIds = intent.getStringArrayListExtra(
					"commentIds");
			final String postId = intent.getStringExtra("postId");

			for(final String commentId : commentIds) {
				mUrls.add(PostCommentListingURL.forPostId(postId).commentId(commentId));
			}

			doRefresh(RefreshableFragment.COMMENTS, false, null);

		} else {
			throw new RuntimeException("Nothing to show! (should load from bundle)"); // TODO
		}
	}

	// TODO save instance state
	// @Override
	// protected void onSaveInstanceState(final Bundle outState) {
	// 	super.onSaveInstanceState(outState);
	// }

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		OptionsMenuUtility.prepare(
				this,
				menu,
				false,
				false,
				true,
				false,
				false,
				false,
				false,
				false,
				false,
				null,
				false,
				false,
				null,
				null);

		if(mFragment != null) {
			mFragment.onCreateOptionsMenu(menu);
		}

		return true;
	}

	@Override
	public void onRedditAccountChanged() {
		requestRefresh(RefreshableFragment.ALL, false);
	}

	@Override
	protected void doRefresh(
			final RefreshableFragment which,
			final boolean force,
			final Bundle savedInstanceState) {

		mFragment = new CommentListingFragment(
				this,
				savedInstanceState,
				mUrls,
				null,
				mSearchString,
				force);

		mFragment.setBaseActivityContent(this);

		setTitle("More Comments");
	}

	@Override
	public void onRefreshComments() {
		requestRefresh(RefreshableFragment.COMMENTS, true);
	}

	@Override
	public void onPastComments() {
	}

	@Override
	public void onSortSelected(final PostCommentSort order) {
	}

	@Override
	public void onSortSelected(final UserCommentSort order) {
	}

	@Override
	public void onSearchComments() {
		DialogUtils.showSearchDialog(this, query -> {
			final Intent searchIntent = getIntent();
			searchIntent.putExtra(EXTRA_SEARCH_STRING, query);
			startActivity(searchIntent);
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {

		if(mFragment != null && mFragment.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, post.src.getUrl(), false, post.src.getSrc());
	}

	@Override
	public void onPostCommentsSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(
				this,
				PostCommentListingURL.forPostId(post.src.getIdAlone()).toString(),
				false);
	}

	@Override
	public void onBackPressed() {
		if(General.onBackPressed()) {
			super.onBackPressed();
		}
	}

	@Override
	public OptionsMenuUtility.Sort getCommentSort() {
		return null;
	}

	@Override
	public PostCommentSort getSuggestedCommentSort() {
		return null;
	}
}
