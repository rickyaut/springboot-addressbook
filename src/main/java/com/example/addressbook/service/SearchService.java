package com.example.addressbook.service;

import com.example.addressbook.model.AddressEntry;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
  private final AddressService addressService;
  private final StandardAnalyzer analyzer = new StandardAnalyzer();
  @Value("${app.lucene.index.path:./data/lucene-index}")
  private String indexPath;

  public void indexAddress(AddressEntry address) throws IOException {
    try (FSDirectory directory = FSDirectory.open(Paths.get(indexPath));
         IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
      
      Document doc = new Document();
      doc.add(new StringField("id", address.getId().toString(), Field.Store.YES));
      doc.add(new TextField("name", address.getName() != null ? address.getName() : "", Field.Store.YES));
      doc.add(new TextField("email", address.getEmail() != null ? address.getEmail() : "", Field.Store.YES));
      doc.add(new TextField("street", address.getStreet() != null ? address.getStreet() : "", Field.Store.YES));
      doc.add(new TextField("phone", address.getPhone() != null ? address.getPhone() : "", Field.Store.YES));
      
      writer.updateDocument(new Term("id", address.getId().toString()), doc);
    }
  }

  public List<AddressEntry> search(String queryString) throws Exception {
    try (FSDirectory directory = FSDirectory.open(Paths.get(indexPath));
         DirectoryReader reader = DirectoryReader.open(directory)) {
      
      IndexSearcher searcher = new IndexSearcher(reader);
      
      // Create fuzzy queries for typo tolerance
      BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
      String[] fields = {"name", "email", "street", "phone"};
      
      for (String field : fields) {
        FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(field, queryString.toLowerCase()), 2);
        booleanQuery.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
      }
      
      Query query = booleanQuery.build();
      TopDocs results = searcher.search(query, 100);
      
      List<AddressEntry> addresses = new ArrayList<>();
      for (ScoreDoc scoreDoc : results.scoreDocs) {
        Document doc = searcher.doc(scoreDoc.doc);
        Long id = Long.parseLong(doc.get("id"));
        AddressEntry address = addressService.findById(id);
        if (address != null) {
          addresses.add(address);
        }
      }
      return addresses;
    }
  }

  public void deleteFromIndex(Long addressId) throws IOException {
    try (FSDirectory directory = FSDirectory.open(Paths.get(indexPath));
         IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
      writer.deleteDocuments(new Term("id", addressId.toString()));
    }
  }
}