<?php echo '<?xml version="1.0" encoding="utf-8"?>'; ?>
<feed xmlns="http://www.w3.org/2005/Atom">
    <title>Jobeet (<?php echo $category ?>)</title>
    <subtitle>Latest Jobs</subtitle>
    <link href="<?php echo url_for('category', array('sf_subject' => $category, 'sf_format' => 'atom'), true) ?>" rel="self" />
    <link href="<?php echo url_for('category', array('sf_subject' => $category), true) ?>" />
    <?php if ($category->getLatestPost() != NULL): ?>
    <updated><?php echo gmstrftime('%Y-%m-%dT%H:%M:%SZ', strtotime($category->getLatestPost()->getCreatedAt())) ?></updated>
    <author>
        <name>Jobeet</name>
    </author>
    <id>
        <?php endif; ?>
        <?php echo sha1(url_for('category', array('sf_subject' => $category), true)) ?></id>

    <?php include_partial('sfJobeetJob/list', array('jobs' => $pager->getResults())) ?>
</feed>