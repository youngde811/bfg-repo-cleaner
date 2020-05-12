Gitclean (https://github.com/youngde811/gitclean)
================

_Removes large or troublesome blobs like git-filter-branch does, but faster - and written in Scala_

```
$ gitclean --strip-blobs-bigger-than 1M --replace-text banned.txt repo.git
```

Gitclean is a fork of the BFG Repo Cleaner (https://rtyley.github.io/bfg-repo-cleaner) - a Scala-based tool used to
simplify the cleaning and shrinking of Git repositories. The original was authored by Roberto Tyley; it has fallen into
some disrepair and contains some "features" that are not suitable for a professional engineering environment. Until a
Gitclean-specific website can be created, please visit **https://rtyley.github.io/bfg-repo-cleaner/** for details and
documentation.

At a minimum, Gitclean may be used to:

* Remove extremely large objects in repository history.
* Removing **Passwords, Credentials** and other **Private data**

As previously written, until Gitclean has a proper website, the main documention for BFG still applies : **https://rtyley.github.io/bfg-repo-cleaner/**
